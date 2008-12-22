/*******************************************************************************
* Copyright (c) 2008 CWI.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Jurgen Vinju - initial API and implementation
*******************************************************************************/

package org.eclipse.imp.pdb.facts.type;

import java.util.Map;



/**
 * A Parameter Type can be used to represent an abstract type,
 * i.e. a type that needs to be instantiated with an actual type
 * later.
 */
/*package*/ final class ParameterType extends Type {
	/* package */ final String fName;
	/* package */ final Type fBound;
	
	/* package */ ParameterType(String name, Type bound) {
		fName = name;
		fBound = bound;
	}
	
	/* package */ ParameterType(String name) {
		fName = name;
		fBound = TypeFactory.getInstance().valueType();
	}
	
	@Override
	public boolean isParameterType() {
		return true;
	}
	
	@Override
	public boolean isValueType() {
		return fBound.isValueType();
	}
	
	@Override
	public boolean isBoolType() {
		return fBound.isBoolType();
	}
	
	@Override
	public boolean isDoubleType() {
		return fBound.isDoubleType();
	}
	
	@Override
	public boolean isIntegerType() {
		return fBound.isIntegerType();
	}
	
	@Override
	public boolean isListType() {
		return fBound.isListType();
	}
	
	@Override
	public boolean isMapType() {
		return fBound.isMapType();
	}
	
	@Override
	public boolean isNamedTreeType() {
		return fBound.isNamedTreeType();
	}
	
	@Override
	public boolean isNamedType() {
		return fBound.isNamedType();
	}
	
	@Override
	public boolean isRelationType() {
		return fBound.isRelationType();
	}
	
	@Override
	public boolean isSetType() {
		return fBound.isSetType();
	}
	
	@Override
	public boolean isSourceLocationType() {
		return fBound.isSourceLocationType();
	}
	
	@Override
	public boolean isSourceRangeType() {
		return fBound.isSourceRangeType();
	}
	
	@Override
	public boolean isStringType() {
		return fBound.isStringType();
	}
	
	@Override
	public boolean isTreeNodeType() {
		return fBound.isTreeNodeType();
	}
	
	@Override
	public boolean isTreeType() {
		return fBound.isTreeType();
	}
	
	@Override
	public boolean isTupleType() {
		return fBound.isTupleType();
	}
	
	@Override
	public boolean isVoidType() {
		return fBound.isVoidType();
	}
	
	@Override
	public Type getBound() {
		return fBound;
	}
	
	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public boolean isSubtypeOf(Type other) {
		if (other == this) {
			return true;
		}
		else {
			return fBound.isSubtypeOf(other);
		}
	}

	@Override
	public Type lub(Type other) {
		if (other == this) {
			return this;
		}
		else {
			return getBound().lub(other);
		}
	}
	
	@Override
	public String toString() {
		return fBound.isValueType() ? "&" + fName : "&" + fName + "<:" + fBound.toString();
	}
	
	@Override
	public int hashCode() {
		return 49991 + 49831 * fName.hashCode() + 133020331 * fBound.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ParameterType) {
			ParameterType other = (ParameterType) o;
			return fName.equals(other.fName) && fBound == other.fBound;
		}
		return false;
	}
	
	@Override
	public <T> T accept(ITypeVisitor<T> visitor) {
		return visitor.visitParameter(this);
	}

	@Override
	public void match(Type matched, Map<Type, Type> bindings)
			throws FactTypeError {
		super.match(matched, bindings);
		
		Type earlier = bindings.get(this);
		if (earlier != null) {
			Type lub = earlier.lub(matched);
			if (!lub.isSubtypeOf(getBound())) {
				throw new FactTypeError(matched + " can not be matched with " + earlier);
			}
			else {
				bindings.put(this, lub);
			}
		}
		else {
			bindings.put(this, matched);
		}
	}
	
	@Override
	public Type instantiate(Map<Type, Type> bindings) {
		Type result = bindings.get(this);
		return result != null ? result : this;
	}
}