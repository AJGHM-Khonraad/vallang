package org.rascalmpl.value.io.binary;

import java.io.IOException;
import java.io.OutputStream;

import org.rascalmpl.interpreter.types.NonTerminalType;
import org.rascalmpl.interpreter.types.OverloadedFunctionType;
import org.rascalmpl.value.IBool;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IDateTime;
import org.rascalmpl.value.IInteger;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.INode;
import org.rascalmpl.value.IReal;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.io.binary.util.MapLastWritten;
import org.rascalmpl.value.io.binary.util.PrePostIValueIterator;
import org.rascalmpl.value.io.binary.util.PrePostTypeIterator;
import org.rascalmpl.value.io.binary.util.TrackLastWritten;
import org.rascalmpl.value.io.binary.util.TypeIteratorKind;
import org.rascalmpl.value.io.binary.util.ValueIteratorKind;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.values.ValueFactoryFactory;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;
        	
/**
 * RSFIValueWriter is a binary serializer for IValues and Types. The main public functions is:
 * - writeValue
 */
	        
public class IValueWriter {
    
    public enum CompressionRate {
        None(0,0,0, 0),
        //TypesOnly(10,0,0),
        //ValuesOnly(0,10,10),
        Fast(10,10,10, 1),
        Normal(50,100,50,3),
        Extreme(50,250,100, 6)
        ;

        private final int uriWindow;
        private final int typeWindow;
        private final int valueWindow;
        private int xzMode;

        CompressionRate(int typeWindow, int valueWindow, int uriWindow, int xzMode) {
            this.typeWindow = typeWindow;
            this.valueWindow = valueWindow;
            this.uriWindow = uriWindow;
            this.xzMode = xzMode;
        } 
    }
    
	protected static final byte[] header = { 'R', 'V', 1,0,0 };
	
	static final class CompressionHeader {
	    public static final byte NONE = 0;
	    public static final byte GZIP = 1;
	    public static final byte XZ = 2;
	}
	
	private static <T> TrackLastWritten<T> getWindow(int size) {
	    if (size == 0) {
	        return new TrackLastWritten<T>() {
	            public int howLongAgo(T obj) {
	                return -1;
	            }
	            public void write(T obj) {};
	        };
	    }
	    return new MapLastWritten<>(size * 1024); 
	}
	
	public static void write(OutputStream out, IValue value, CompressionRate compression, boolean shouldClose) throws IOException {
        out.write(header);
    	out.write(compression.typeWindow);
    	out.write(compression.valueWindow);
    	out.write(compression.uriWindow);
    	out.write(compression.xzMode == 0 ? CompressionHeader.NONE : CompressionHeader.XZ);
    	if (compression.xzMode > 0) {
    	    out = new XZOutputStream(out, new LZMA2Options(compression.xzMode));
    	}
    	ValueWireOutputStream writer =  new ValueWireOutputStream(out);
    	try {
    	    TrackLastWritten<Type> typeCache = getWindow(compression.typeWindow);
    	    TrackLastWritten<IValue> valueCache = getWindow(compression.valueWindow);
    	    TrackLastWritten<ISourceLocation> uriCache = getWindow(compression.uriWindow);
    	    write(writer, value, typeCache, valueCache, uriCache);
    	}
    	finally {
    	    writer.flush();
    	    if (shouldClose) {
    	        writer.close();
    	    }
    	    else {
                if (compression.xzMode > 0) {
                    ((XZOutputStream)out).finish();
                }
    	    }
    	}
	}
	
	private static void writeNames(final ValueWireOutputStream writer, int fieldId, String[] names) throws IOException{
		writer.writeField(fieldId, names.length);
		for(int i = 0; i < names.length; i++){
		    writer.writeField(fieldId, names[i]);
		}
	}
	
	private static void write(final ValueWireOutputStream writer, final Type type, final TrackLastWritten<Type> typeCache, final TrackLastWritten<IValue> valueCache, final TrackLastWritten<ISourceLocation> uriCache) throws IOException {
	    final PrePostTypeIterator iter = new PrePostTypeIterator(type);
	    
	    while(iter.hasNext()){
	        final TypeIteratorKind kind = iter.next();
	        final Type currentType = iter.getItem();
	        if (kind.isCompound()) {
                if (iter.atBeginning()) {
                    int lastSeen = typeCache.howLongAgo(currentType);
                    if (lastSeen != -1) { 
                        writeSingleValueMessage(writer, IValueIDs.PreviousType.ID, IValueIDs.PreviousType.HOW_LONG_AGO, lastSeen);
                        iter.skipItem();
                    }
                }
                else {
                    switch(kind){
                        case ADT: {
                            writeSingleValueMessage(writer, IValueIDs.ADTType.ID, IValueIDs.ADTType.NAME, currentType.getName());
                            break;

                        }
                        case ALIAS: {
                            writeSingleValueMessage(writer, IValueIDs.AliasType.ID, IValueIDs.AliasType.NAME, currentType.getName());
                            break;
                        }
                        case CONSTRUCTOR : {
                            writeSingleValueMessage(writer, IValueIDs.ConstructorType.ID, IValueIDs.ConstructorType.NAME, currentType.getName());
                            break;
                        }
                        case FUNCTION: {
                            writer.writeEmptyMessage(IValueIDs.ConstructorType.ID);
                            break;
                        }

                        case REIFIED: {
                            writer.writeEmptyMessage(IValueIDs.ReifiedType.ID);
                            break;
                        }

                        case OVERLOADED: {
                            writeSingleValueMessage(writer, IValueIDs.OverloadedType.ID, IValueIDs.OverloadedType.SIZE, ((OverloadedFunctionType) currentType).getAlternatives().size());
                            break;
                        }

                        case NONTERMINAL: {
                            // first prefix with the Constructor 
                            write(writer, ((NonTerminalType)currentType).getSymbol(), typeCache, valueCache, uriCache);
                            writer.writeEmptyMessage(IValueIDs.NonTerminalType.ID);
                            break;
                        }

                        case LIST: {
                            writer.writeEmptyMessage(IValueIDs.ListType.ID);
                            break;
                        }

                        case MAP: {
                            writer.writeEmptyMessage(IValueIDs.MapType.ID);
                            break;
                        }
                        case PARAMETER: {
                            writeSingleValueMessage(writer, IValueIDs.ParameterType.ID, IValueIDs.ParameterType.NAME,currentType.getName());
                            break;
                        }

                        case SET: {
                            writer.writeEmptyMessage(IValueIDs.SetType.ID);
                            break;
                        }
                        case TUPLE: {
                            writer.startMessage(IValueIDs.TupleType.ID);
                            writer.writeField(IValueIDs.TupleType.ARITY, currentType.getArity());
                            String[] fieldNames = currentType.getFieldNames();
                            if(fieldNames != null){
                                writeNames(writer, IValueIDs.TupleType.NAMES, fieldNames);
                            }
                            writer.endMessage();
                            break;
                        }
                        default:
                            throw new RuntimeException("Missing compound type case");
                    }
                    typeCache.write(currentType);
                }
	        }
	        else {
	            switch(kind){
	                case BOOL: {
	                    writer.writeEmptyMessage(IValueIDs.BoolType.ID);
	                    break;
	                }
	                case DATETIME: {
	                    writer.writeEmptyMessage(IValueIDs.DateTimeType.ID);
	                    break;
	                }
	                case INT: {
	                    writer.writeEmptyMessage(IValueIDs.IntegerType.ID);
	                    break;
	                }
	                case NODE: {
	                    writer.writeEmptyMessage(IValueIDs.NodeType.ID);
	                    break;
	                }
	                case NUMBER: {
	                    writer.writeEmptyMessage(IValueIDs.NumberType.ID);
	                    break;
	                }
	                case RATIONAL: {
	                    writer.writeEmptyMessage(IValueIDs.RationalType.ID);
	                    break;
	                }
	                case REAL: {
	                    writer.writeEmptyMessage(IValueIDs.RealType.ID);
	                    break;
	                }
	                case LOC: {
	                    writer.writeEmptyMessage(IValueIDs.SourceLocationType.ID);
	                    break;
	                }
	                case STR: {
	                    writer.writeEmptyMessage(IValueIDs.StringType.ID);
	                    break;
	                }
	                case VALUE: {
	                    writer.writeEmptyMessage(IValueIDs.ValueType.ID);
	                    break;
	                }
	                case VOID: {
	                    writer.writeEmptyMessage(IValueIDs.VoidType.ID);
	                    break;
	                }
                    default:
                        throw new RuntimeException("Missing non-compound type case");

	            }
	        }
	    }
	}
	
	private static void writeSingleValueMessage(final ValueWireOutputStream writer, int messageID, int fieldId, long fieldValue) throws IOException {
	    writer.startMessage(messageID);
	    writer.writeField(fieldId, fieldValue);
	    writer.endMessage();
	}
	
	private static void writeSingleValueMessage(final ValueWireOutputStream writer, int messageID, int fieldId, String fieldValue) throws IOException {
	    writer.startMessage(messageID);
	    writer.writeField(fieldId, fieldValue);
	    writer.endMessage();
	}

	private static final IInteger MININT =ValueFactoryFactory.getValueFactory().integer(Integer.MIN_VALUE);
	private static final IInteger MAXINT =ValueFactoryFactory.getValueFactory().integer(Integer.MAX_VALUE);
	
	private static void write(final ValueWireOutputStream writer, final IValue value, final TrackLastWritten<Type> typeCache, final TrackLastWritten<IValue> valueCache, final TrackLastWritten<ISourceLocation> uriCache) throws IOException {
		PrePostIValueIterator iter = new PrePostIValueIterator(value);
		
		while(iter.hasNext()){
			final ValueIteratorKind kind = iter.next();
			final IValue currentValue = iter.getItem();
			if (kind.isCompound()) {
			    if (iter.atBeginning()) {
			        int lastSeen = valueCache.howLongAgo(currentValue);
			        if (lastSeen != -1) {
			            writeSingleValueMessage(writer, IValueIDs.PreviousValue.ID, IValueIDs.PreviousValue.HOW_FAR_BACK, lastSeen);
			            iter.skipItem();
			        }
			    }
			    else {
			        switch(kind){
			            case CONSTRUCTOR: {
			                IConstructor cons = (IConstructor)currentValue;
			                write(writer, cons.getUninstantiatedConstructorType(), typeCache, valueCache, uriCache);

			                writer.startMessage(IValueIDs.ConstructorValue.ID);
			                writer.writeField(IValueIDs.ConstructorValue.ARITY, cons.arity());
			                if(cons.mayHaveKeywordParameters()){
			                    if(cons.asWithKeywordParameters().hasParameters()){
			                        writer.writeField(IValueIDs.ConstructorValue.KWPARAMS, cons.asWithKeywordParameters().getParameters().size());
			                    }
			                } else {
			                    if(cons.asAnnotatable().hasAnnotations()){
			                        writer.writeField(IValueIDs.ConstructorValue.ANNOS, cons.asAnnotatable().getAnnotations().size());
			                    }
			                }
			                writer.endMessage();
			                break;
			            }


			            case LIST: {
			                writeSingleValueMessage(writer, IValueIDs.ListValue.ID, IValueIDs.ListValue.SIZE, ((IList)currentValue).length());
			                break;
			            }

			            case MAP: {
			                writeSingleValueMessage(writer, IValueIDs.MapValue.ID, IValueIDs.MapValue.SIZE, ((IMap)currentValue).size());
			                break;
			            }
			            case SET: {
			                writeSingleValueMessage(writer, IValueIDs.SetValue.ID, IValueIDs.SetValue.SIZE, ((ISet)currentValue).size());
			                break;
			            }

			            case NODE: {
			                INode node = (INode)currentValue;
			                writer.startMessage(IValueIDs.NodeValue.ID);
			                writer.writeField(IValueIDs.NodeValue.NAME,  node.getName());
			                writer.writeField(IValueIDs.NodeValue.ARITY, node.arity());
			                if(node.mayHaveKeywordParameters()){
			                    if(node.asWithKeywordParameters().hasParameters()){
			                        writer.writeField(IValueIDs.NodeValue.KWPARAMS, node.asWithKeywordParameters().getParameters().size());
			                    }
			                } else {
			                    if(node.asAnnotatable().hasAnnotations()){
			                        writer.writeField(IValueIDs.NodeValue.ANNOS, node.asAnnotatable().getAnnotations().size());
			                    }
			                }
			                writer.endMessage();
			                break;
			            }

			            case RATIONAL: {
			                writer.writeEmptyMessage(IValueIDs.RationalValue.ID);
			                break;
			            }

			            case TUPLE: {
			                writeSingleValueMessage(writer, IValueIDs.TupleValue.ID, IValueIDs.TupleValue.SIZE, ((ITuple)currentValue).arity());
			                break;
			            }

			            default:
			                throw new RuntimeException("writeValue: unexpected kind of value " + kind);
			        }
			        valueCache.write(currentValue);
			    }
			}
			else {
			    assert iter.atBeginning();
			    switch(kind){
			        case BOOL: {
			            writeSingleValueMessage(writer, IValueIDs.BoolValue.ID, IValueIDs.BoolValue.VALUE, ((IBool)currentValue).getValue() ? 1: 0);
			            break;
			        }

			        case DATETIME: {
			            IDateTime dateTime = (IDateTime)currentValue;
			            writer.startMessage(IValueIDs.DateTimeValue.ID);

			            if (!dateTime.isTime()) {
			                writer.writeField(IValueIDs.DateTimeValue.YEAR, dateTime.getYear());
			                writer.writeField(IValueIDs.DateTimeValue.MONTH, dateTime.getMonthOfYear());
			                writer.writeField(IValueIDs.DateTimeValue.DAY, dateTime.getDayOfMonth());
			            }

			            if (!dateTime.isDate()) {
			                writer.writeField(IValueIDs.DateTimeValue.HOUR, dateTime.getHourOfDay());
			                writer.writeField(IValueIDs.DateTimeValue.MINUTE, dateTime.getMinuteOfHour());
			                writer.writeField(IValueIDs.DateTimeValue.SECOND, dateTime.getSecondOfMinute());
			                writer.writeField(IValueIDs.DateTimeValue.MILLISECOND, dateTime.getMillisecondsOfSecond());

			                writer.writeField(IValueIDs.DateTimeValue.TZ_HOUR, dateTime.getTimezoneOffsetHours());
			                writer.writeField(IValueIDs.DateTimeValue.TZ_MINUTE, dateTime.getTimezoneOffsetMinutes());
			            }
			            writer.endMessage();
			            break;
			        }

			        case INT: {
			            writer.startMessage(IValueIDs.IntegerValue.ID);
			            IInteger ii = (IInteger)currentValue;
			            if(ii. greaterEqual(MININT).getValue() && ii.lessEqual(MAXINT).getValue()){
			                writer.writeField(IValueIDs.IntegerValue.INTVALUE, ii.intValue());
			            } 
			            else {
			                writer.writeField(IValueIDs.IntegerValue.BIGVALUE, ii.getTwosComplementRepresentation());
			            }
			            writer.endMessage();
			            break;
			        }


			        case REAL: {
			            writer.startMessage(IValueIDs.RealValue.ID);
			            writer.writeField(IValueIDs.RealValue.CONTENT, ((IReal)currentValue).unscaled().getTwosComplementRepresentation());
			            writer.writeField(IValueIDs.RealValue.SCALE, ((IReal)currentValue).scale());
			            writer.endMessage();
			            break;
			        }

			        case LOC: {
			            writer.startMessage(IValueIDs.SourceLocationValue.ID);
			            ISourceLocation loc = (ISourceLocation)currentValue;
			            ISourceLocation uriPart = loc.top();
			            int alreadyWritten = uriCache.howLongAgo(uriPart);
			            if (alreadyWritten == -1) {
			                writer.writeField(IValueIDs.SourceLocationValue.SCHEME, uriPart.getScheme());
			                if (uriPart.hasAuthority()) {
			                    writer.writeField(IValueIDs.SourceLocationValue.AUTHORITY, uriPart.getAuthority());
			                }
			                if (uriPart.hasPath()) {
			                    writer.writeField(IValueIDs.SourceLocationValue.PATH, uriPart.getPath());
			                }
			                if (uriPart.hasQuery()) {
			                    writer.writeField(IValueIDs.SourceLocationValue.QUERY,  uriPart.getQuery());
			                }
			                if (uriPart.hasFragment()) {
			                    writer.writeField(IValueIDs.SourceLocationValue.FRAGMENT,  uriPart.getFragment());
			                }
			                uriCache.write(uriPart);
			            }
			            else {
			                writer.writeField(IValueIDs.SourceLocationValue.PREVIOUS_URI, alreadyWritten);
			            }

			            if(loc.hasOffsetLength()){
			                writer.writeField(IValueIDs.SourceLocationValue.OFFSET, loc.getOffset());
			                writer.writeField(IValueIDs.SourceLocationValue.LENGTH, loc.getLength());
			            } 
			            if(loc.hasLineColumn()){
			                writer.writeField(IValueIDs.SourceLocationValue.BEGINLINE, loc.getBeginLine());
			                writer.writeField(IValueIDs.SourceLocationValue.ENDLINE, loc.getEndLine());
			                writer.writeField(IValueIDs.SourceLocationValue.BEGINCOLUMN, loc.getBeginColumn());
			                writer.writeField(IValueIDs.SourceLocationValue.ENDCOLUMN, loc.getEndColumn());
			            }
			            writer.endMessage();
			            break;
			        }

			        case STR: {
			            writeSingleValueMessage(writer, IValueIDs.StringValue.ID, IValueIDs.StringValue.CONTENT, ((IString)currentValue).getValue());
			            break;
			        }

			        default:
			            throw new RuntimeException("writeValue: unexpected kind of value " + kind);
			    }
			}
		}
	}
}
