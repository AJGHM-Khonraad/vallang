package org.rascalmpl.value.io.binary.util;

import java.io.IOException;
import java.util.Map;

import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.INode;
import org.rascalmpl.value.IRational;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.visitors.NullVisitor;
import org.rascalmpl.values.ValueFactoryFactory;

public class PrePostIValueIterator  {
    
    private final PositionStack stack;
    private IValue value;
    private boolean beginning;
    private boolean hasEnd;
    private IValueFactory vf = ValueFactoryFactory.getValueFactory();

    public PrePostIValueIterator(IValue root) {
        this(root, 1024);
    }

    public PrePostIValueIterator(IValue root, int stackSize) {
        stack = new PositionStack(stackSize);
        stack.push(root, true);
        beginning = false; // start out at fake end
        hasEnd = false;
    }

    public boolean hasNext() {
        return !stack.isEmpty() || (beginning && hasEnd);
    }

    
    public IValue skipValue() {
        assert beginning;
        beginning = false;
        return value;
    }

    public boolean atBeginning() {
        return beginning;
    }


    public IValue getValue() {
        return value;
    }


    public IValue next() throws IOException {
        if (beginning) {
            if (hasEnd) {
                stack.push(value, false);
                value.accept(new NullVisitor<Void, RuntimeException>() {
                    @Override
                    public Void visitConstructor(IConstructor cons) throws RuntimeException {
                        if(cons.mayHaveKeywordParameters()){
                            if(cons.asWithKeywordParameters().hasParameters()){
                                for (Map.Entry<String, IValue> param : cons.asWithKeywordParameters().getParameters().entrySet()) {
                                    IString key = vf.string(param.getKey());
                                    IValue val = param.getValue();
                                    stack.push(val, true);
                                    stack.push(key, true);
                                }
                            }
                        } else {
                            if(cons.asAnnotatable().hasAnnotations()){
                                for (Map.Entry<String, IValue> param : cons.asAnnotatable().getAnnotations().entrySet()) {
                                    IString key = vf.string(param.getKey());
                                    IValue val = param.getValue();
                                    stack.push(val, true);
                                    stack.push(key, true);
                                }
                            }
                        }

                        for(int i = cons.arity() - 1; i >= 0; i--){
                            IValue elem = cons.get(i);
                            stack.push(elem, true);
                        }
                        return null;
                    }
                    @Override
                    public Void visitList(IList lst) throws RuntimeException {
                        for (int i = lst.length() - 1; i >= 0; i--) {
                            IValue elem = lst.get(i);
                            stack.push(elem, true);
                        }
                        return null;
                    }
                    @Override
                    public Void visitMap(IMap map) throws RuntimeException {
                        for(IValue key : map){
                            IValue val = map.get(key);
                            stack.push(val, true);
                            stack.push(key, true);
                        }
                        return null;
                    }
                    @Override
                    public Void visitNode(INode node) throws RuntimeException {
                        if(node.mayHaveKeywordParameters()){
                            if(node.asWithKeywordParameters().hasParameters()){
                                for (Map.Entry<String, IValue> param : node.asWithKeywordParameters().getParameters().entrySet()) {
                                    IString key = vf.string(param.getKey());
                                    IValue val = param.getValue();
                                    stack.push(val, true);
                                    stack.push(key, true);
                                }
                            }
                        } else {
                            if(node.asAnnotatable().hasAnnotations()){
                                for (Map.Entry<String, IValue> param : node.asAnnotatable().getAnnotations().entrySet()) {
                                    IString key = vf.string(param.getKey());
                                    IValue val = param.getValue();
                                    stack.push(val, true);
                                    stack.push(key, true);
                                }
                            }
                        }

                        for(int i = node.arity() - 1; i >= 0; i--){
                            IValue elem = node.get(i);
                            stack.push(elem, true);
                        }
                        return null;
                    }
                    @Override
                    public Void visitRational(IRational rat) throws RuntimeException {
                        stack.push(rat.denominator(), true);
                        stack.push(rat.numerator(), true);
                        return null;
                    }
                    @Override
                    public Void visitSet(ISet set) throws RuntimeException {
                        for(IValue elem : set){
                            stack.push(elem, true);
                        }
                        return null;
                    }
                    @Override
                    public Void visitTuple(ITuple tuple) throws RuntimeException {
                        for(int i = tuple.arity() - 1; i >= 0; i--){
                            IValue elem = tuple.get(i);
                            stack.push(elem, true);
                        }
                        return null;
                    }
                });
            }
        }
        value = stack.currentIValue();
        beginning = stack.currentBeginning();
        hasEnd = beginning && hasEnd(value);
        stack.pop();
        return value;
    }


    private static boolean hasEnd(IValue val) {
        return val instanceof IConstructor
           || val instanceof IList
           || val instanceof ISet
           || val instanceof IMap
           || val instanceof INode
           || val instanceof IRational
           || val instanceof ITuple
           ;
    }

}