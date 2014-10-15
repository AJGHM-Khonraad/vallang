/*******************************************************************************
 * Copyright (c) 2013-2014 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.eclipse.imp.pdb.facts.util;

import static org.eclipse.imp.pdb.facts.util.AbstractSpecialisedImmutableMap.entryOf;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("rawtypes")
public class TrieMap_5Bits_IntKey<V> extends AbstractMap<java.lang.Integer, V> implements
				ImmutableMap<java.lang.Integer, V> {

	@SuppressWarnings("unchecked")
	private static final TrieMap_5Bits_IntKey EMPTY_MAP = new TrieMap_5Bits_IntKey(
					CompactMapNode.EMPTY_NODE, 0, 0);

	private static final boolean DEBUG = false;

	private final AbstractMapNode<V> rootNode;
	private final int hashCode;
	private final int cachedSize;

	TrieMap_5Bits_IntKey(AbstractMapNode<V> rootNode, int hashCode, int cachedSize) {
		this.rootNode = rootNode;
		this.hashCode = hashCode;
		this.cachedSize = cachedSize;
		if (DEBUG) {
			assert checkHashCodeAndSize(hashCode, cachedSize);
		}
	}

	@SuppressWarnings("unchecked")
	public static final <V> ImmutableMap<java.lang.Integer, V> of() {
		return TrieMap_5Bits_IntKey.EMPTY_MAP;
	}

	@SuppressWarnings("unchecked")
	public static final <V> ImmutableMap<java.lang.Integer, V> of(Object... keyValuePairs) {
		if (keyValuePairs.length % 2 != 0) {
			throw new IllegalArgumentException(
							"Length of argument list is uneven: no key/value pairs.");
		}

		ImmutableMap<java.lang.Integer, V> result = TrieMap_5Bits_IntKey.EMPTY_MAP;

		for (int i = 0; i < keyValuePairs.length; i += 2) {
			final int key = (int) keyValuePairs[i];
			final V val = (V) keyValuePairs[i + 1];

			result = result.__put(key, val);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static final <V> TransientMap<java.lang.Integer, V> transientOf() {
		return TrieMap_5Bits_IntKey.EMPTY_MAP.asTransient();
	}

	@SuppressWarnings("unchecked")
	public static final <V> TransientMap<java.lang.Integer, V> transientOf(Object... keyValuePairs) {
		if (keyValuePairs.length % 2 != 0) {
			throw new IllegalArgumentException(
							"Length of argument list is uneven: no key/value pairs.");
		}

		final TransientMap<java.lang.Integer, V> result = TrieMap_5Bits_IntKey.EMPTY_MAP
						.asTransient();

		for (int i = 0; i < keyValuePairs.length; i += 2) {
			final int key = (int) keyValuePairs[i];
			final V val = (V) keyValuePairs[i + 1];

			result.__put(key, val);
		}

		return result;
	}

	private boolean checkHashCodeAndSize(final int targetHash, final int targetSize) {
		int hash = 0;
		int size = 0;

		for (Iterator<Map.Entry<java.lang.Integer, V>> it = entryIterator(); it.hasNext();) {
			final Map.Entry<java.lang.Integer, V> entry = it.next();
			final int key = entry.getKey();
			final V val = entry.getValue();

			hash += (int) key ^ val.hashCode();
			size += 1;
		}

		return hash == targetHash && size == targetSize;
	}

	@Override
	public TrieMap_5Bits_IntKey<V> __put(final java.lang.Integer key, final V val) {
		final int keyHash = key.hashCode();
		final Result<V> details = Result.unchanged();

		final CompactMapNode<V> newRootNode = rootNode.updated(null, key, val, keyHash, 0, details);

		if (details.isModified()) {

			if (details.hasReplacedValue()) {
				final int valHashOld = details.getReplacedValue().hashCode();
				final int valHashNew = val.hashCode();

				return new TrieMap_5Bits_IntKey<V>(newRootNode, hashCode + (keyHash ^ valHashNew)
								- (keyHash ^ valHashOld), cachedSize);
			}

			final int valHash = val.hashCode();
			return new TrieMap_5Bits_IntKey<V>(newRootNode, hashCode + (keyHash ^ valHash),
							cachedSize + 1);

		}

		return this;
	}

	@Override
	public TrieMap_5Bits_IntKey<V> __putEquivalent(final java.lang.Integer key, final V val,
					final Comparator<Object> cmp) {
		final int keyHash = key.hashCode();
		final Result<V> details = Result.unchanged();

		final CompactMapNode<V> newRootNode = rootNode.updated(null, key, val, keyHash, 0, details,
						cmp);

		if (details.isModified()) {

			if (details.hasReplacedValue()) {
				final int valHashOld = details.getReplacedValue().hashCode();
				final int valHashNew = val.hashCode();

				return new TrieMap_5Bits_IntKey<V>(newRootNode, hashCode + (keyHash ^ valHashNew)
								- (keyHash ^ valHashOld), cachedSize);
			}

			final int valHash = val.hashCode();
			return new TrieMap_5Bits_IntKey<V>(newRootNode, hashCode + (keyHash ^ valHash),
							cachedSize + 1);

		}

		return this;
	}

	@Override
	public ImmutableMap<java.lang.Integer, V> __remove(final java.lang.Integer key) {
		final int keyHash = key.hashCode();
		final Result<V> details = Result.unchanged();

		final CompactMapNode<V> newRootNode = rootNode.removed(null, key, keyHash, 0, details);

		if (details.isModified()) {

			assert details.hasReplacedValue();
			final int valHash = details.getReplacedValue().hashCode();

			return new TrieMap_5Bits_IntKey<V>(newRootNode, hashCode - (keyHash ^ valHash),
							cachedSize - 1);

		}

		return this;
	}

	@Override
	public ImmutableMap<java.lang.Integer, V> __removeEquivalent(final java.lang.Integer key,
					final Comparator<Object> cmp) {
		final int keyHash = key.hashCode();
		final Result<V> details = Result.unchanged();

		final CompactMapNode<V> newRootNode = rootNode.removed(null, key, keyHash, 0, details, cmp);

		if (details.isModified()) {

			assert details.hasReplacedValue();
			final int valHash = details.getReplacedValue().hashCode();

			return new TrieMap_5Bits_IntKey<V>(newRootNode, hashCode - (keyHash ^ valHash),
							cachedSize - 1);

		}

		return this;
	}

	@Override
	public boolean containsKey(final java.lang.Object o) {
		try {
			@SuppressWarnings("unchecked")
			final int key = (int) o;
			return rootNode.containsKey(key, (int) key, 0);
		} catch (ClassCastException unused) {
			return false;
		}
	}

	@Override
	public boolean containsKeyEquivalent(final java.lang.Object o, final Comparator<Object> cmp) {
		try {
			@SuppressWarnings("unchecked")
			final int key = (int) o;
			return rootNode.containsKey(key, (int) key, 0, cmp);
		} catch (ClassCastException unused) {
			return false;
		}
	}

	@Override
	public boolean containsValue(final java.lang.Object o) {
		for (Iterator<V> iterator = valueIterator(); iterator.hasNext();) {
			if (iterator.next().equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValueEquivalent(final java.lang.Object o, final Comparator<Object> cmp) {
		for (Iterator<V> iterator = valueIterator(); iterator.hasNext();) {
			if (iterator.next().equals(o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(final java.lang.Object o) {
		try {
			@SuppressWarnings("unchecked")
			final int key = (int) o;
			final Optional<V> result = rootNode.findByKey(key, (int) key, 0);

			if (result.isPresent()) {
				return result.get();
			} else {
				return null;
			}
		} catch (ClassCastException unused) {
			return null;
		}
	}

	@Override
	public V getEquivalent(final java.lang.Object o, final Comparator<Object> cmp) {
		try {
			@SuppressWarnings("unchecked")
			final int key = (int) o;
			final Optional<V> result = rootNode.findByKey(key, (int) key, 0, cmp);

			if (result.isPresent()) {
				return result.get();
			} else {
				return null;
			}
		} catch (ClassCastException unused) {
			return null;
		}
	}

	@Override
	public ImmutableMap<java.lang.Integer, V> __putAll(
					final Map<? extends java.lang.Integer, ? extends V> map) {
		TransientMap<java.lang.Integer, V> tmp = asTransient();
		tmp.__putAll(map);
		return tmp.freeze();
	}

	@Override
	public ImmutableMap<java.lang.Integer, V> __putAllEquivalent(
					final Map<? extends java.lang.Integer, ? extends V> map,
					final Comparator<Object> cmp) {
		TransientMap<java.lang.Integer, V> tmp = asTransient();
		tmp.__putAllEquivalent(map, cmp);
		return tmp.freeze();
	}

	@Override
	public V put(final java.lang.Integer key, final V val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(final java.lang.Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(final Map<? extends java.lang.Integer, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return cachedSize;
	}

	@Override
	public SupplierIterator<java.lang.Integer, V> keyIterator() {
		return new MapKeyIterator<>(rootNode);
	}

	@Override
	public Iterator<V> valueIterator() {
		return new MapValueIterator<>(rootNode);
	}

	@Override
	public Iterator<Map.Entry<java.lang.Integer, V>> entryIterator() {
		return new MapEntryIterator<>(rootNode);
	}

	@Override
	public Set<java.util.Map.Entry<java.lang.Integer, V>> entrySet() {
		Set<java.util.Map.Entry<java.lang.Integer, V>> entrySet = null;

		if (entrySet == null) {
			entrySet = new AbstractSet<java.util.Map.Entry<java.lang.Integer, V>>() {
				@Override
				public Iterator<java.util.Map.Entry<java.lang.Integer, V>> iterator() {
					return new Iterator<Entry<java.lang.Integer, V>>() {
						private final Iterator<Entry<java.lang.Integer, V>> i = entryIterator();

						@Override
						public boolean hasNext() {
							return i.hasNext();
						}

						@Override
						public Entry<java.lang.Integer, V> next() {
							return i.next();
						}

						@Override
						public void remove() {
							i.remove();
						}
					};
				}

				@Override
				public int size() {
					return TrieMap_5Bits_IntKey.this.size();
				}

				@Override
				public boolean isEmpty() {
					return TrieMap_5Bits_IntKey.this.isEmpty();
				}

				@Override
				public void clear() {
					TrieMap_5Bits_IntKey.this.clear();
				}

				@Override
				public boolean contains(Object k) {
					return TrieMap_5Bits_IntKey.this.containsKey(k);
				}
			};
		}
		return entrySet;
	}

	@Override
	public boolean isTransientSupported() {
		return true;
	}

	@Override
	public TransientMap<java.lang.Integer, V> asTransient() {
		return new TransientTrieMap_5Bits_IntKey<V>(this);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		if (other instanceof TrieMap_5Bits_IntKey) {
			TrieMap_5Bits_IntKey<?> that = (TrieMap_5Bits_IntKey<?>) other;

			if (this.size() != that.size()) {
				return false;
			}

			return rootNode.equals(that.rootNode);
		}

		return super.equals(other);
	}

	/*
	 * For analysis purposes only.
	 */
	protected AbstractMapNode<V> getRootNode() {
		return rootNode;
	}

	/*
	 * For analysis purposes only.
	 */
	protected Iterator<AbstractMapNode<V>> nodeIterator() {
		return new TrieMap_5Bits_IntKeyNodeIterator<>(rootNode);
	}

	/*
	 * For analysis purposes only.
	 */
	protected int getNodeCount() {
		final Iterator<AbstractMapNode<V>> it = nodeIterator();
		int sumNodes = 0;

		for (; it.hasNext(); it.next()) {
			sumNodes += 1;
		}

		return sumNodes;
	}

	/*
	 * For analysis purposes only. Payload X Node
	 */
	protected int[][] arityCombinationsHistogram() {
		final Iterator<AbstractMapNode<V>> it = nodeIterator();
		final int[][] sumArityCombinations = new int[33][33];

		while (it.hasNext()) {
			final AbstractMapNode<V> node = it.next();
			sumArityCombinations[node.payloadArity()][node.nodeArity()] += 1;
		}

		return sumArityCombinations;
	}

	/*
	 * For analysis purposes only.
	 */
	protected int[] arityHistogram() {
		final int[][] sumArityCombinations = arityCombinationsHistogram();
		final int[] sumArity = new int[33];

		final int maxArity = 32; // TODO: factor out constant

		for (int j = 0; j <= maxArity; j++) {
			for (int maxRestArity = maxArity - j, k = 0; k <= maxRestArity - j; k++) {
				sumArity[j + k] += sumArityCombinations[j][k];
			}
		}

		return sumArity;
	}

	/*
	 * For analysis purposes only.
	 */
	public void printStatistics() {
		final int[][] sumArityCombinations = arityCombinationsHistogram();
		final int[] sumArity = arityHistogram();
		final int sumNodes = getNodeCount();

		final int[] cumsumArity = new int[33];
		for (int cumsum = 0, i = 0; i < 33; i++) {
			cumsum += sumArity[i];
			cumsumArity[i] = cumsum;
		}

		final float threshhold = 0.01f; // for printing results
		for (int i = 0; i < 33; i++) {
			float arityPercentage = (float) (sumArity[i]) / sumNodes;
			float cumsumArityPercentage = (float) (cumsumArity[i]) / sumNodes;

			if (arityPercentage != 0 && arityPercentage >= threshhold) {
				// details per level
				StringBuilder bldr = new StringBuilder();
				int max = i;
				for (int j = 0; j <= max; j++) {
					for (int k = max - j; k <= max - j; k++) {
						float arityCombinationsPercentage = (float) (sumArityCombinations[j][k])
										/ sumNodes;

						if (arityCombinationsPercentage != 0
										&& arityCombinationsPercentage >= threshhold) {
							bldr.append(String.format("%d/%d: %s, ", j, k, new DecimalFormat(
											"0.00%").format(arityCombinationsPercentage)));
						}
					}
				}
				final String detailPercentages = bldr.toString();

				// overview
				System.out.println(String.format("%2d: %s\t[cumsum = %s]\t%s", i,
								new DecimalFormat("0.00%").format(arityPercentage),
								new DecimalFormat("0.00%").format(cumsumArityPercentage),
								detailPercentages));
			}
		}
	}

	abstract static class Optional<T> {
		private static final Optional EMPTY = new Optional() {
			@Override
			boolean isPresent() {
				return false;
			}

			@Override
			Object get() {
				return null;
			}
		};

		@SuppressWarnings("unchecked")
		static <T> Optional<T> empty() {
			return EMPTY;
		}

		static <T> Optional<T> of(T value) {
			return new Value<T>(value);
		}

		abstract boolean isPresent();

		abstract T get();

		private static final class Value<T> extends Optional<T> {
			private final T value;

			private Value(T value) {
				this.value = value;
			}

			@Override
			boolean isPresent() {
				return true;
			}

			@Override
			T get() {
				return value;
			}
		}
	}

	static final class Result<V> {
		private V replacedValue;
		private boolean isModified;
		private boolean isReplaced;

		// update: inserted/removed single element, element count changed
		public void modified() {
			this.isModified = true;
		}

		public void updated(V replacedValue) {
			this.replacedValue = replacedValue;
			this.isModified = true;
			this.isReplaced = true;
		}

		// update: neither element, nor element count changed
		public static <V> Result<V> unchanged() {
			return new Result<>();
		}

		private Result() {
		}

		public boolean isModified() {
			return isModified;
		}

		public boolean hasReplacedValue() {
			return isReplaced;
		}

		public V getReplacedValue() {
			return replacedValue;
		}
	}

	protected static abstract class AbstractNode<K, V> {
	}

	protected static abstract class AbstractMapNode<V> extends AbstractNode<java.lang.Integer, V> {

		static final int TUPLE_LENGTH = 2;

		abstract boolean containsKey(final int key, final int keyHash, final int shift);

		abstract boolean containsKey(final int key, final int keyHash, final int shift,
						final Comparator<Object> cmp);

		abstract Optional<V> findByKey(final int key, final int keyHash, final int shift);

		abstract Optional<V> findByKey(final int key, final int keyHash, final int shift,
						final Comparator<Object> cmp);

		abstract CompactMapNode<V> updated(final AtomicReference<Thread> mutator, final int key,
						final V val, final int keyHash, final int shift, final Result<V> details);

		abstract CompactMapNode<V> updated(final AtomicReference<Thread> mutator, final int key,
						final V val, final int keyHash, final int shift, final Result<V> details,
						final Comparator<Object> cmp);

		abstract CompactMapNode<V> removed(final AtomicReference<Thread> mutator, final int key,
						final int keyHash, final int shift, final Result<V> details);

		abstract CompactMapNode<V> removed(final AtomicReference<Thread> mutator, final int key,
						final int keyHash, final int shift, final Result<V> details,
						final Comparator<Object> cmp);

		static final boolean isAllowedToEdit(AtomicReference<Thread> x, AtomicReference<Thread> y) {
			return x != null && y != null && (x == y || x.get() == y.get());
		}

		abstract AbstractMapNode<V> getNode(final int index);

		abstract boolean hasNodes();

		abstract int nodeArity();

		@Deprecated
		Iterator<? extends AbstractMapNode<V>> nodeIterator() {
			return new Iterator<AbstractMapNode<V>>() {

				int nextIndex = 0;

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public AbstractMapNode<V> next() {
					if (!hasNext())
						throw new NoSuchElementException();
					return AbstractMapNode.this.getNode(nextIndex++);
				}

				@Override
				public boolean hasNext() {
					return nextIndex < AbstractMapNode.this.nodeArity();
				}
			};
		}

		abstract int getKey(final int index);

		abstract V getValue(final int index);

		abstract java.util.Map.Entry<java.lang.Integer, V> getKeyValueEntry(final int index);

		abstract boolean hasPayload();

		abstract int payloadArity();

		@Deprecated
		/**
		 * The arity of this trie node (i.e. number of values and nodes stored
		 * on this level).
		 * 
		 * @return sum of nodes and values stored within
		 */
		int arity() {
			return payloadArity() + nodeArity();
		}

		int size() {
			final SupplierIterator<java.lang.Integer, V> it = new MapKeyIterator<>(this);

			int size = 0;
			while (it.hasNext()) {
				size += 1;
				it.next();
			}

			return size;
		}

	}

	private static abstract class CompactMapNode<V> extends AbstractMapNode<V> {

		static final int BIT_PARTITION_SIZE = 5;
		static final int BIT_PARTITION_MASK = 0b11111;

		abstract int nodeMap();

		abstract int dataMap();

		static final byte SIZE_EMPTY = 0b00;
		static final byte SIZE_ONE = 0b01;
		static final byte SIZE_MORE_THAN_ONE = 0b10;

		/**
		 * Abstract predicate over a node's size. Value can be either
		 * {@value #SIZE_EMPTY}, {@value #SIZE_ONE}, or
		 * {@value #SIZE_MORE_THAN_ONE}.
		 * 
		 * @return size predicate
		 */
		abstract byte sizePredicate();

		@Override
		abstract CompactMapNode<V> getNode(final int index);

		boolean nodeInvariant() {
			boolean inv1 = (size() - payloadArity() >= 2 * (arity() - payloadArity()));
			boolean inv2 = (this.arity() == 0) ? sizePredicate() == SIZE_EMPTY : true;
			boolean inv3 = (this.arity() == 1 && payloadArity() == 1) ? sizePredicate() == SIZE_ONE
							: true;
			boolean inv4 = (this.arity() >= 2) ? sizePredicate() == SIZE_MORE_THAN_ONE : true;

			boolean inv5 = (this.nodeArity() >= 0) && (this.payloadArity() >= 0)
							&& ((this.payloadArity() + this.nodeArity()) == this.arity());

			return inv1 && inv2 && inv3 && inv4 && inv5;
		}

		abstract CompactMapNode<V> copyAndSetValue(AtomicReference<Thread> mutator,
						final int bitpos, final V val);

		abstract CompactMapNode<V> copyAndInsertValue(AtomicReference<Thread> mutator,
						final int bitpos, final int key, final V val);

		abstract CompactMapNode<V> copyAndRemoveValue(AtomicReference<Thread> mutator,
						final int bitpos);

		abstract CompactMapNode<V> copyAndSetNode(AtomicReference<Thread> mutator,
						final int bitpos, CompactMapNode<V> node);

		abstract CompactMapNode<V> copyAndMigrateFromInlineToNode(
						final AtomicReference<Thread> mutator, final int bitpos,
						final CompactMapNode<V> node);

		abstract CompactMapNode<V> copyAndMigrateFromNodeToInline(
						final AtomicReference<Thread> mutator, final int bitpos,
						final CompactMapNode<V> node);

		/*
		 * TODO: specialize removed(..) to remove this method from this
		 * interface
		 */

		@SuppressWarnings("unchecked")
		static final <V> CompactMapNode<V> mergeNodes(final int key0, final V val0, int keyHash0,
						final int key1, final V val1, int keyHash1, int shift) {
			assert !(key0 == key1);

			if (keyHash0 == keyHash1) {
				return new HashCollisionMapNode_5Bits_IntKey<>(keyHash0, (int[]) new int[] { key0,
								key1 }, (V[]) new Object[] { val0, val1 });
			}

			final int mask0 = (keyHash0 >>> shift) & BIT_PARTITION_MASK;
			final int mask1 = (keyHash1 >>> shift) & BIT_PARTITION_MASK;

			if (mask0 != mask1) {
				// both nodes fit on same level
				final int dataMap = (int) (1L << mask0 | 1L << mask1);

				if (mask0 < mask1) {
					return nodeOf(null, (int) 0, dataMap, new Object[] { key0, val0, key1, val1 },
									(byte) 2);
				} else {
					return nodeOf(null, (int) 0, dataMap, new Object[] { key1, val1, key0, val0 },
									(byte) 2);
				}
			} else {
				// values fit on next level
				final CompactMapNode<V> node = mergeNodes(key0, val0, keyHash0, key1, val1,
								keyHash1, shift + BIT_PARTITION_SIZE);

				final int nodeMap = (int) (1L << mask0);
				return nodeOf(null, nodeMap, (int) 0, new Object[] { node }, (byte) 0);
			}
		}

		static final <V> CompactMapNode<V> mergeNodes(CompactMapNode<V> node0, int keyHash0,
						final int key1, final V val1, int keyHash1, int shift) {
			final int mask0 = (keyHash0 >>> shift) & BIT_PARTITION_MASK;
			final int mask1 = (keyHash1 >>> shift) & BIT_PARTITION_MASK;

			if (mask0 != mask1) {
				// both nodes fit on same level
				final int nodeMap = (int) (1L << mask0);
				final int dataMap = (int) (1L << mask1);

				// store values before node
				return nodeOf(null, nodeMap, dataMap, new Object[] { key1, val1, node0 }, (byte) 1);
			} else {
				// values fit on next level
				final CompactMapNode<V> node = mergeNodes(node0, keyHash0, key1, val1, keyHash1,
								shift + BIT_PARTITION_SIZE);

				final int nodeMap = (int) (1L << mask0);
				return nodeOf(null, nodeMap, (int) 0, new Object[] { node }, (byte) 0);
			}
		}

		static final CompactMapNode EMPTY_NODE;

		static {
			EMPTY_NODE = new BitmapIndexedMapNode<>(null, (int) 0, (int) 0, new Object[] {},
							(byte) 0);
		};

		static final <V> CompactMapNode<V> nodeOf(AtomicReference<Thread> mutator,
						final int nodeMap, final int dataMap, Object[] nodes, byte payloadArity) {
			return new BitmapIndexedMapNode<>(mutator, nodeMap, dataMap, nodes, payloadArity);
		}

		@SuppressWarnings("unchecked")
		static final <V> CompactMapNode<V> nodeOf(AtomicReference<Thread> mutator) {
			return EMPTY_NODE;
		}

		static final <V> CompactMapNode<V> nodeOf(AtomicReference<Thread> mutator,
						final int nodeMap, final int dataMap, final int key, final V val) {
			assert nodeMap == 0;
			return nodeOf(mutator, (int) 0, dataMap, new Object[] { key, val }, (byte) 1);
		}

		final int dataIndex(final int bitpos) {
			return java.lang.Integer.bitCount(dataMap() & (bitpos - 1));
		}

		final int nodeIndex(final int bitpos) {
			return java.lang.Integer.bitCount(nodeMap() & (bitpos - 1));
		}

		int keyAt(final int bitpos) {
			return getKey(dataIndex(bitpos));
		}

		V valAt(final int bitpos) {
			return getValue(dataIndex(bitpos));
		}

		CompactMapNode<V> nodeAt(final int bitpos) {
			return getNode(nodeIndex(bitpos));
		}

		@Override
		boolean containsKey(final int key, final int keyHash, final int shift) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) {
				return keyAt(bitpos) == key;
			}

			if ((nodeMap() & bitpos) != 0) {
				return nodeAt(bitpos).containsKey(key, keyHash, shift + BIT_PARTITION_SIZE);
			}

			return false;
		}

		@Override
		boolean containsKey(final int key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) {
				return keyAt(bitpos) == key;
			}

			if ((nodeMap() & bitpos) != 0) {
				return nodeAt(bitpos).containsKey(key, keyHash, shift + BIT_PARTITION_SIZE, cmp);
			}

			return false;
		}

		@Override
		Optional<V> findByKey(final int key, final int keyHash, final int shift) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				if (keyAt(bitpos) == key) {
					final V _val = valAt(bitpos);

					return Optional.of(_val);
				}

				return Optional.empty();
			}

			if ((nodeMap() & bitpos) != 0) { // node (not value)
				final AbstractMapNode<V> subNode = nodeAt(bitpos);

				return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE);
			}

			return Optional.empty();
		}

		@Override
		Optional<V> findByKey(final int key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				if (keyAt(bitpos) == key) {
					final V _val = valAt(bitpos);

					return Optional.of(_val);
				}

				return Optional.empty();
			}

			if ((nodeMap() & bitpos) != 0) { // node (not value)
				final AbstractMapNode<V> subNode = nodeAt(bitpos);

				return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE, cmp);
			}

			return Optional.empty();
		}

		@Override
		CompactMapNode<V> updated(final AtomicReference<Thread> mutator, final int key,
						final V val, final int keyHash, final int shift, final Result<V> details) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);
				final int currentKey = getKey(dataIndex);

				if (currentKey == key) {
					final V currentVal = getValue(dataIndex);

					if (currentVal.equals(val)) {
						return this;
					} else {
						// update mapping
						details.updated(currentVal);
						return copyAndSetValue(mutator, bitpos, val);
					}
				} else {
					final V currentVal = getValue(dataIndex);
					final CompactMapNode<V> subNodeNew = mergeNodes(currentKey, currentVal,
									(int) currentKey, key, val, keyHash, shift + BIT_PARTITION_SIZE);

					details.modified();
					return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);

				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactMapNode<V> subNode = nodeAt(bitpos);
				final CompactMapNode<V> subNodeNew = subNode.updated(mutator, key, val, keyHash,
								shift + BIT_PARTITION_SIZE, details);

				if (details.isModified()) {
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				} else {
					return this;
				}
			} else {
				// no value
				details.modified();
				return copyAndInsertValue(mutator, bitpos, key, val);
			}
		}

		@Override
		CompactMapNode<V> updated(final AtomicReference<Thread> mutator, final int key,
						final V val, final int keyHash, final int shift, final Result<V> details,
						final Comparator<Object> cmp) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);
				final int currentKey = getKey(dataIndex);

				if (currentKey == key) {
					final V currentVal = getValue(dataIndex);

					if (cmp.compare(currentVal, val) == 0) {
						return this;
					} else {
						// update mapping
						details.updated(currentVal);
						return copyAndSetValue(mutator, bitpos, val);
					}
				} else {
					final V currentVal = getValue(dataIndex);
					final CompactMapNode<V> subNodeNew = mergeNodes(currentKey, currentVal,
									(int) currentKey, key, val, keyHash, shift + BIT_PARTITION_SIZE);

					details.modified();
					return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);

				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactMapNode<V> subNode = nodeAt(bitpos);
				final CompactMapNode<V> subNodeNew = subNode.updated(mutator, key, val, keyHash,
								shift + BIT_PARTITION_SIZE, details, cmp);

				if (details.isModified()) {
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				} else {
					return this;
				}
			} else {
				// no value
				details.modified();
				return copyAndInsertValue(mutator, bitpos, key, val);
			}
		}

		@Override
		CompactMapNode<V> removed(final AtomicReference<Thread> mutator, final int key,
						final int keyHash, final int shift, final Result<V> details) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);

				if (getKey(dataIndex) == key) {
					final V currentVal = getValue(dataIndex);
					details.updated(currentVal);

					if (this.payloadArity() == 2 && this.nodeArity() == 0) {
						/*
						 * Create new node with remaining pair. The new node
						 * will a) either become the new root returned, or b)
						 * unwrapped and inlined during returning.
						 */
						final int newDataMap = (shift == 0) ? (int) (dataMap() ^ bitpos)
										: (int) (1L << (keyHash & BIT_PARTITION_MASK));

						if (dataIndex == 0) {
							return CompactMapNode.<V> nodeOf(mutator, (int) 0, newDataMap,
											getKey(1), getValue(1));
						} else {
							return CompactMapNode.<V> nodeOf(mutator, (int) 0, newDataMap,
											getKey(0), getValue(0));
						}
					} else {
						return copyAndRemoveValue(mutator, bitpos);
					}
				} else {
					return this;
				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactMapNode<V> subNode = nodeAt(bitpos);
				final CompactMapNode<V> subNodeNew = subNode.removed(mutator, key, keyHash, shift
								+ BIT_PARTITION_SIZE, details);

				if (!details.isModified()) {
					return this;
				}

				switch (subNodeNew.sizePredicate()) {
				case 0: {
					throw new IllegalStateException("Sub-node must have at least one element.");
				}
				case 1: {
					if (this.payloadArity() == 0 && this.nodeArity() == 1) {
						// escalate (singleton or empty) result
						return subNodeNew;
					} else {
						// inline value (move to front)
						return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
					}
				}
				default: {
					// modify current node (set replacement node)
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				}
				}
			}

			return this;
		}

		@Override
		CompactMapNode<V> removed(final AtomicReference<Thread> mutator, final int key,
						final int keyHash, final int shift, final Result<V> details,
						final Comparator<Object> cmp) {
			final int mask = (keyHash >>> shift) & BIT_PARTITION_MASK;
			final int bitpos = (int) (1L << mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);

				if (getKey(dataIndex) == key) {
					final V currentVal = getValue(dataIndex);
					details.updated(currentVal);

					if (this.payloadArity() == 2 && this.nodeArity() == 0) {
						/*
						 * Create new node with remaining pair. The new node
						 * will a) either become the new root returned, or b)
						 * unwrapped and inlined during returning.
						 */
						final int newDataMap = (shift == 0) ? (int) (dataMap() ^ bitpos)
										: (int) (1L << (keyHash & BIT_PARTITION_MASK));

						if (dataIndex == 0) {
							return CompactMapNode.<V> nodeOf(mutator, (int) 0, newDataMap,
											getKey(1), getValue(1));
						} else {
							return CompactMapNode.<V> nodeOf(mutator, (int) 0, newDataMap,
											getKey(0), getValue(0));
						}
					} else {
						return copyAndRemoveValue(mutator, bitpos);
					}
				} else {
					return this;
				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactMapNode<V> subNode = nodeAt(bitpos);
				final CompactMapNode<V> subNodeNew = subNode.removed(mutator, key, keyHash, shift
								+ BIT_PARTITION_SIZE, details, cmp);

				if (!details.isModified()) {
					return this;
				}

				switch (subNodeNew.sizePredicate()) {
				case 0: {
					throw new IllegalStateException("Sub-node must have at least one element.");
				}
				case 1: {
					if (this.payloadArity() == 0 && this.nodeArity() == 1) {
						// escalate (singleton or empty) result
						return subNodeNew;
					} else {
						// inline value (move to front)
						return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
					}
				}
				default: {
					// modify current node (set replacement node)
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				}
				}
			}

			return this;
		}

		/**
		 * @return 0 <= mask <= 2^BIT_PARTITION_SIZE - 1
		 */
		static byte recoverMask(int map, byte i_th) {
			assert 1 <= i_th && i_th <= 32;

			byte cnt1 = 0;
			byte mask = 0;

			while (mask < 32) {
				if ((map & 0x01) == 0x01) {
					cnt1 += 1;

					if (cnt1 == i_th) {
						return mask;
					}
				}

				map = (int) (map >> 1);
				mask += 1;
			}

			assert cnt1 != i_th;
			throw new RuntimeException("Called with invalid arguments.");
		}

		@Override
		public String toString() {
			final StringBuilder bldr = new StringBuilder();
			bldr.append('[');

			for (byte i = 0; i < payloadArity(); i++) {
				final byte pos = recoverMask(dataMap(), (byte) (i + 1));
				bldr.append(String.format("@%d: %s", pos, getKey(i), getValue(i)));

				if (!((i + 1) == payloadArity())) {
					bldr.append(", ");
				}
			}

			if (payloadArity() > 0 && nodeArity() > 0) {
				bldr.append(", ");
			}

			for (byte i = 0; i < nodeArity(); i++) {
				final byte pos = recoverMask(nodeMap(), (byte) (i + 1));
				bldr.append(String.format("@%d: %s", pos, getNode(i)));

				if (!((i + 1) == nodeArity())) {
					bldr.append(", ");
				}
			}

			bldr.append(']');
			return bldr.toString();
		}

	}

	private static abstract class CompactMixedMapNode<V> extends CompactMapNode<V> {

		private final int nodeMap;
		private final int dataMap;

		CompactMixedMapNode(final AtomicReference<Thread> mutator, final int nodeMap,
						final int dataMap) {
			this.nodeMap = nodeMap;
			this.dataMap = dataMap;
		}

		@Override
		public int nodeMap() {
			return nodeMap;
		}

		@Override
		public int dataMap() {
			return dataMap;
		}

	}

	private static final class BitmapIndexedMapNode<V> extends CompactMixedMapNode<V> {
		private AtomicReference<Thread> mutator;

		private Object[] nodes;
		final private byte payloadArity;

		BitmapIndexedMapNode(AtomicReference<Thread> mutator, final int nodeMap, final int dataMap,
						Object[] nodes, byte payloadArity) {
			super(mutator, nodeMap, dataMap);

			assert (TUPLE_LENGTH * java.lang.Integer.bitCount(dataMap)
							+ java.lang.Integer.bitCount(nodeMap) == nodes.length);

			this.mutator = mutator;

			this.nodes = nodes;
			this.payloadArity = payloadArity;

			assert (payloadArity == java.lang.Integer.bitCount(dataMap));
			// assert (payloadArity() >= 2 || nodeArity() >= 1); // =
			// // SIZE_MORE_THAN_ONE

			// for (int i = 0; i < TUPLE_LENGTH * payloadArity; i++)
			// assert ((nodes[i] instanceof CompactNode) == false);
			//
			// for (int i = TUPLE_LENGTH * payloadArity; i < nodes.length; i++)
			// assert ((nodes[i] instanceof CompactNode) == true);

			// assert invariant
			assert nodeInvariant();
		}

		@Override
		int getKey(int index) {
			return (int) nodes[TUPLE_LENGTH * index];
		}

		@SuppressWarnings("unchecked")
		@Override
		V getValue(int index) {
			return (V) nodes[TUPLE_LENGTH * index + 1];
		}

		@SuppressWarnings("unchecked")
		@Override
		Map.Entry<java.lang.Integer, V> getKeyValueEntry(int index) {
			return entryOf((int) nodes[TUPLE_LENGTH * index], (V) nodes[TUPLE_LENGTH * index + 1]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public CompactMapNode<V> getNode(int index) {
			final int offset = TUPLE_LENGTH * payloadArity;
			return (CompactMapNode<V>) nodes[offset + index];
		}

		@SuppressWarnings("unchecked")
		@Override
		Iterator<CompactMapNode<V>> nodeIterator() {
			final int offset = TUPLE_LENGTH * payloadArity;

			for (int i = offset; i < nodes.length - offset; i++) {
				assert ((nodes[i] instanceof AbstractMapNode) == true);
			}

			return (Iterator) ArrayIterator.of(nodes, offset, nodes.length - offset);
		}

		@Override
		boolean hasPayload() {
			return payloadArity != 0;
		}

		@Override
		int payloadArity() {
			return payloadArity;
		}

		@Override
		boolean hasNodes() {
			return TUPLE_LENGTH * payloadArity != nodes.length;
		}

		@Override
		int nodeArity() {
			return nodes.length - TUPLE_LENGTH * payloadArity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 0;
			result = prime * result + ((int) dataMap());
			result = prime * result + ((int) dataMap());
			result = prime * result + Arrays.hashCode(nodes);
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if (null == other) {
				return false;
			}
			if (this == other) {
				return true;
			}
			if (getClass() != other.getClass()) {
				return false;
			}
			BitmapIndexedMapNode<?> that = (BitmapIndexedMapNode<?>) other;
			if (nodeMap() != that.nodeMap()) {
				return false;
			}
			if (dataMap() != that.dataMap()) {
				return false;
			}
			if (!Arrays.equals(nodes, that.nodes)) {
				return false;
			}
			return true;
		}

		@Override
		byte sizePredicate() {
			if (this.nodeArity() == 0 && this.payloadArity == 0) {
				return SIZE_EMPTY;
			} else if (this.nodeArity() == 0 && this.payloadArity == 1) {
				return SIZE_ONE;
			} else {
				return SIZE_MORE_THAN_ONE;
			}
		}

		@Override
		CompactMapNode<V> copyAndSetValue(AtomicReference<Thread> mutator, final int bitpos,
						final V val) {
			final int idx = TUPLE_LENGTH * dataIndex(bitpos) + 1;

			if (isAllowedToEdit(this.mutator, mutator)) {
				// no copying if already editable
				this.nodes[idx] = val;
				return this;
			} else {
				final java.lang.Object[] src = this.nodes;
				final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length];

				// copy 'src' and set 1 element(s) at position 'idx'
				System.arraycopy(src, 0, dst, 0, src.length);
				dst[idx + 0] = val;

				return nodeOf(mutator, nodeMap(), dataMap(), dst, payloadArity);
			}
		}

		@Override
		CompactMapNode<V> copyAndSetNode(AtomicReference<Thread> mutator, final int bitpos,
						CompactMapNode<V> node) {
			final int idx = TUPLE_LENGTH * payloadArity + nodeIndex(bitpos);

			if (isAllowedToEdit(this.mutator, mutator)) {
				// no copying if already editable
				this.nodes[idx] = node;
				return this;
			} else {
				final java.lang.Object[] src = this.nodes;
				final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length];

				// copy 'src' and set 1 element(s) at position 'idx'
				System.arraycopy(src, 0, dst, 0, src.length);
				dst[idx + 0] = node;

				return nodeOf(mutator, nodeMap(), dataMap(), dst, payloadArity);
			}
		}

		@Override
		CompactMapNode<V> copyAndInsertValue(AtomicReference<Thread> mutator, final int bitpos,
						final int key, final V val) {
			final int idx = TUPLE_LENGTH * dataIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length + 2];

			// copy 'src' and insert 2 element(s) at position 'idx'
			System.arraycopy(src, 0, dst, 0, idx);
			dst[idx + 0] = key;
			dst[idx + 1] = val;
			System.arraycopy(src, idx, dst, idx + 2, src.length - idx);

			return nodeOf(mutator, nodeMap(), (int) (dataMap() | bitpos), dst,
							(byte) (payloadArity + 1));
		}

		@Override
		CompactMapNode<V> copyAndRemoveValue(AtomicReference<Thread> mutator, final int bitpos) {
			final int idx = TUPLE_LENGTH * dataIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length - 2];

			// copy 'src' and remove 2 element(s) at position 'idx'
			System.arraycopy(src, 0, dst, 0, idx);
			System.arraycopy(src, idx + 2, dst, idx, src.length - idx - 2);

			return nodeOf(mutator, nodeMap(), (int) (dataMap() ^ bitpos), dst,
							(byte) (payloadArity - 1));
		}

		@Override
		CompactMapNode<V> copyAndMigrateFromInlineToNode(AtomicReference<Thread> mutator,
						final int bitpos, CompactMapNode<V> node) {
			final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
			final int idxNew = TUPLE_LENGTH * (payloadArity - 1) + nodeIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = new Object[src.length - 2 + 1];

			// copy 'src' and remove 2 element(s) at position 'idxOld' and
			// insert 1 element(s) at position 'idxNew' (TODO: carefully test)
			assert idxOld <= idxNew;
			System.arraycopy(src, 0, dst, 0, idxOld);
			System.arraycopy(src, idxOld + 2, dst, idxOld, idxNew - idxOld);
			dst[idxNew + 0] = node;
			System.arraycopy(src, idxNew + 2, dst, idxNew + 1, src.length - idxNew - 2);

			return nodeOf(mutator, (int) (nodeMap() | bitpos), (int) (dataMap() ^ bitpos), dst,
							(byte) (payloadArity - 1));
		}

		@Override
		CompactMapNode<V> copyAndMigrateFromNodeToInline(AtomicReference<Thread> mutator,
						final int bitpos, CompactMapNode<V> node) {
			final int idxOld = TUPLE_LENGTH * payloadArity + nodeIndex(bitpos);
			final int idxNew = dataIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = new Object[src.length - 1 + 2];

			// copy 'src' and remove 1 element(s) at position 'idxOld' and
			// insert 2 element(s) at position 'idxNew' (TODO: carefully test)
			assert idxOld >= idxNew;
			System.arraycopy(src, 0, dst, 0, idxNew);
			dst[idxNew + 0] = node.getKey(0);
			dst[idxNew + 1] = node.getValue(0);
			System.arraycopy(src, idxNew, dst, idxNew + 2, idxOld - idxNew);
			System.arraycopy(src, idxOld + 1, dst, idxOld + 2, src.length - idxOld - 1);

			return nodeOf(mutator, (int) (nodeMap() ^ bitpos), (int) (dataMap() | bitpos), dst,
							(byte) (payloadArity + 1));
		}

	}

	private static final class HashCollisionMapNode_5Bits_IntKey<V> extends CompactMapNode<V> {
		private final int[] keys;
		private final V[] vals;
		private final int hash;

		HashCollisionMapNode_5Bits_IntKey(final int hash, final int[] keys, final V[] vals) {
			this.keys = keys;
			this.vals = vals;
			this.hash = hash;

			assert payloadArity() >= 2;
		}

		@Override
		boolean containsKey(final int key, final int keyHash, final int shift) {

			if (this.hash == keyHash) {
				for (int k : keys) {
					if (k == key) {
						return true;
					}
				}
			}
			return false;

		}

		@Override
		boolean containsKey(final int key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {

			if (this.hash == keyHash) {
				for (int k : keys) {
					if (k == key) {
						return true;
					}
				}
			}
			return false;

		}

		@Override
		Optional<V> findByKey(final int key, final int keyHash, final int shift) {

			for (int i = 0; i < keys.length; i++) {
				final int _key = keys[i];
				if (key == _key) {
					final V _val = vals[i];
					return Optional.of(_val);
				}
			}
			return Optional.empty();

		}

		@Override
		Optional<V> findByKey(final int key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {

			for (int i = 0; i < keys.length; i++) {
				final int _key = keys[i];
				if (key == _key) {
					final V _val = vals[i];
					return Optional.of(_val);
				}
			}
			return Optional.empty();

		}

		@Override
		CompactMapNode<V> updated(final AtomicReference<Thread> mutator, final int key,
						final V val, final int keyHash, final int shift, final Result<V> details) {
			if (this.hash != keyHash) {
				details.modified();
				return mergeNodes(this, this.hash, key, val, keyHash, shift);
			}

			for (int idx = 0; idx < keys.length; idx++) {
				if (keys[idx] == key) {

					final V currentVal = vals[idx];

					if (currentVal.equals(val)) {
						return this;
					}

					final V[] src = this.vals;
					@SuppressWarnings("unchecked")
					final V[] dst = (V[]) new Object[src.length];

					// copy 'src' and set 1 element(s) at position 'idx'
					System.arraycopy(src, 0, dst, 0, src.length);
					dst[idx + 0] = val;

					final CompactMapNode<V> thisNew = new HashCollisionMapNode_5Bits_IntKey<>(
									this.hash, this.keys, dst);

					details.updated(currentVal);
					return thisNew;

				}
			}

			final int[] keysNew = new int[this.keys.length + 1];

			// copy 'this.keys' and insert 1 element(s) at position
			// 'keys.length'
			System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
			keysNew[keys.length + 0] = key;
			System.arraycopy(this.keys, keys.length, keysNew, keys.length + 1, this.keys.length
							- keys.length);

			@SuppressWarnings("unchecked")
			final V[] valsNew = (V[]) new Object[this.vals.length + 1];

			// copy 'this.vals' and insert 1 element(s) at position
			// 'vals.length'
			System.arraycopy(this.vals, 0, valsNew, 0, vals.length);
			valsNew[vals.length + 0] = val;
			System.arraycopy(this.vals, vals.length, valsNew, vals.length + 1, this.vals.length
							- vals.length);

			details.modified();
			return new HashCollisionMapNode_5Bits_IntKey<>(keyHash, keysNew, valsNew);
		}

		@Override
		CompactMapNode<V> updated(final AtomicReference<Thread> mutator, final int key,
						final V val, final int keyHash, final int shift, final Result<V> details,
						final Comparator<Object> cmp) {
			if (this.hash != keyHash) {
				details.modified();
				return mergeNodes(this, this.hash, key, val, keyHash, shift);
			}

			for (int idx = 0; idx < keys.length; idx++) {
				if (keys[idx] == key) {

					final V currentVal = vals[idx];

					if (cmp.compare(currentVal, val) == 0) {
						return this;
					}

					final V[] src = this.vals;
					@SuppressWarnings("unchecked")
					final V[] dst = (V[]) new Object[src.length];

					// copy 'src' and set 1 element(s) at position 'idx'
					System.arraycopy(src, 0, dst, 0, src.length);
					dst[idx + 0] = val;

					final CompactMapNode<V> thisNew = new HashCollisionMapNode_5Bits_IntKey<>(
									this.hash, this.keys, dst);

					details.updated(currentVal);
					return thisNew;

				}
			}

			final int[] keysNew = new int[this.keys.length + 1];

			// copy 'this.keys' and insert 1 element(s) at position
			// 'keys.length'
			System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
			keysNew[keys.length + 0] = key;
			System.arraycopy(this.keys, keys.length, keysNew, keys.length + 1, this.keys.length
							- keys.length);

			@SuppressWarnings("unchecked")
			final V[] valsNew = (V[]) new Object[this.vals.length + 1];

			// copy 'this.vals' and insert 1 element(s) at position
			// 'vals.length'
			System.arraycopy(this.vals, 0, valsNew, 0, vals.length);
			valsNew[vals.length + 0] = val;
			System.arraycopy(this.vals, vals.length, valsNew, vals.length + 1, this.vals.length
							- vals.length);

			details.modified();
			return new HashCollisionMapNode_5Bits_IntKey<>(keyHash, keysNew, valsNew);
		}

		@Override
		CompactMapNode<V> removed(final AtomicReference<Thread> mutator, final int key,
						final int keyHash, final int shift, final Result<V> details) {

			for (int idx = 0; idx < keys.length; idx++) {
				if (keys[idx] == key) {
					final V currentVal = vals[idx];
					details.updated(currentVal);

					if (this.arity() == 1) {
						return nodeOf(mutator);
					} else if (this.arity() == 2) {
						/*
						 * Create root node with singleton element. This node
						 * will be a) either be the new root returned, or b)
						 * unwrapped and inlined.
						 */
						final int theOtherKey = (idx == 0) ? keys[1] : keys[0];
						final V theOtherVal = (idx == 0) ? vals[1] : vals[0];
						return CompactMapNode.<V> nodeOf(mutator).updated(mutator, theOtherKey,
										theOtherVal, keyHash, 0, details);
					} else {
						final int[] keysNew = new int[this.keys.length - 1];

						// copy 'this.keys' and remove 1 element(s) at position
						// 'idx'
						System.arraycopy(this.keys, 0, keysNew, 0, idx);
						System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx
										- 1);

						@SuppressWarnings("unchecked")
						final V[] valsNew = (V[]) new Object[this.vals.length - 1];

						// copy 'this.vals' and remove 1 element(s) at position
						// 'idx'
						System.arraycopy(this.vals, 0, valsNew, 0, idx);
						System.arraycopy(this.vals, idx + 1, valsNew, idx, this.vals.length - idx
										- 1);

						return new HashCollisionMapNode_5Bits_IntKey<>(keyHash, keysNew, valsNew);
					}
				}
			}
			return this;

		}

		@Override
		CompactMapNode<V> removed(final AtomicReference<Thread> mutator, final int key,
						final int keyHash, final int shift, final Result<V> details,
						final Comparator<Object> cmp) {

			for (int idx = 0; idx < keys.length; idx++) {
				if (keys[idx] == key) {
					final V currentVal = vals[idx];
					details.updated(currentVal);

					if (this.arity() == 1) {
						return nodeOf(mutator);
					} else if (this.arity() == 2) {
						/*
						 * Create root node with singleton element. This node
						 * will be a) either be the new root returned, or b)
						 * unwrapped and inlined.
						 */
						final int theOtherKey = (idx == 0) ? keys[1] : keys[0];
						final V theOtherVal = (idx == 0) ? vals[1] : vals[0];
						return CompactMapNode.<V> nodeOf(mutator).updated(mutator, theOtherKey,
										theOtherVal, keyHash, 0, details, cmp);
					} else {
						final int[] keysNew = new int[this.keys.length - 1];

						// copy 'this.keys' and remove 1 element(s) at position
						// 'idx'
						System.arraycopy(this.keys, 0, keysNew, 0, idx);
						System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx
										- 1);

						@SuppressWarnings("unchecked")
						final V[] valsNew = (V[]) new Object[this.vals.length - 1];

						// copy 'this.vals' and remove 1 element(s) at position
						// 'idx'
						System.arraycopy(this.vals, 0, valsNew, 0, idx);
						System.arraycopy(this.vals, idx + 1, valsNew, idx, this.vals.length - idx
										- 1);

						return new HashCollisionMapNode_5Bits_IntKey<>(keyHash, keysNew, valsNew);
					}
				}
			}
			return this;

		}

		@Override
		boolean hasPayload() {
			return true;
		}

		@Override
		int payloadArity() {
			return keys.length;
		}

		@Override
		boolean hasNodes() {
			return false;
		}

		@Override
		int nodeArity() {
			return 0;
		}

		@Override
		int arity() {
			return payloadArity();
		}

		@Override
		byte sizePredicate() {
			return SIZE_MORE_THAN_ONE;
		}

		@Override
		int getKey(int index) {
			return keys[index];
		}

		@Override
		V getValue(int index) {
			return vals[index];
		}

		@Override
		Map.Entry<java.lang.Integer, V> getKeyValueEntry(int index) {
			return entryOf(keys[index], vals[index]);
		}

		@Override
		public CompactMapNode<V> getNode(int index) {
			throw new IllegalStateException("Is leaf node.");
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 0;
			result = prime * result + hash;
			result = prime * result + Arrays.hashCode(keys);
			result = prime * result + Arrays.hashCode(vals);
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if (null == other) {
				return false;
			}
			if (this == other) {
				return true;
			}
			if (getClass() != other.getClass()) {
				return false;
			}

			HashCollisionMapNode_5Bits_IntKey<?> that = (HashCollisionMapNode_5Bits_IntKey<?>) other;

			if (hash != that.hash) {
				return false;
			}

			if (arity() != that.arity()) {
				return false;
			}

			/*
			 * Linear scan for each key, because of arbitrary element order.
			 */
			outerLoop: for (int i = 0; i < that.payloadArity(); i++) {
				final int otherKey = that.getKey(i);
				final java.lang.Object otherVal = that.getValue(i);

				for (int j = 0; j < keys.length; j++) {
					final int key = keys[j];
					final V val = vals[j];

					if (key == otherKey && val.equals(otherVal)) {
						continue outerLoop;
					}
				}
				return false;
			}

			return true;
		}

		@Override
		CompactMapNode<V> copyAndSetValue(AtomicReference<Thread> mutator, final int bitpos,
						final V val) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactMapNode<V> copyAndInsertValue(AtomicReference<Thread> mutator, final int bitpos,
						final int key, final V val) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactMapNode<V> copyAndRemoveValue(AtomicReference<Thread> mutator, final int bitpos) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactMapNode<V> copyAndSetNode(AtomicReference<Thread> mutator, final int bitpos,
						CompactMapNode<V> node) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactMapNode<V> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
						final int bitpos, final CompactMapNode<V> node) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactMapNode<V> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
						final int bitpos, final CompactMapNode<V> node) {
			throw new UnsupportedOperationException();
		}

		@Override
		int nodeMap() {
			throw new UnsupportedOperationException();
		}

		@Override
		int dataMap() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Iterator skeleton that uses a fixed stack in depth.
	 */
	private static abstract class AbstractMapIterator<V> {

		// TODO: verify maximum deepness
		private static final int MAX_DEPTH = 8;

		protected int currentValueCursor;
		protected int currentValueLength;
		protected AbstractMapNode<V> currentValueNode;

		private int currentStackLevel;
		private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];

		@SuppressWarnings("unchecked")
		AbstractMapNode<V>[] nodes = new AbstractMapNode[MAX_DEPTH];

		AbstractMapIterator(AbstractMapNode<V> rootNode) {
			currentStackLevel = 0;

			currentValueNode = rootNode;
			currentValueCursor = 0;
			currentValueLength = rootNode.payloadArity();

			nodes[0] = rootNode;
			nodeCursorsAndLengths[0] = 0;
			nodeCursorsAndLengths[1] = rootNode.nodeArity();
		}

		public boolean hasNext() {
			if (currentValueCursor < currentValueLength) {
				return true;
			} else {
				/*
				 * search for next node that contains values
				 */
				while (currentStackLevel >= 0) {
					final int currentCursorIndex = currentStackLevel * 2;
					final int currentLengthIndex = currentCursorIndex + 1;

					final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
					final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

					if (nodeCursor < nodeLength) {
						final AbstractMapNode<V> nextNode = nodes[currentStackLevel]
										.getNode(nodeCursor);
						nodeCursorsAndLengths[currentCursorIndex]++;

						final int nextValueLength = nextNode.payloadArity();
						final int nextNodeLength = nextNode.nodeArity();

						if (nextNodeLength > 0) {
							/*
							 * put node on next stack level for depth-first
							 * traversal
							 */
							final int nextStackLevel = ++currentStackLevel;
							final int nextCursorIndex = nextStackLevel * 2;
							final int nextLengthIndex = nextCursorIndex + 1;

							nodes[nextStackLevel] = nextNode;
							nodeCursorsAndLengths[nextCursorIndex] = 0;
							nodeCursorsAndLengths[nextLengthIndex] = nextNodeLength;
						}

						if (nextValueLength != 0) {
							/*
							 * found for next node that contains values
							 */
							currentValueNode = nextNode;
							currentValueCursor = 0;
							currentValueLength = nextValueLength;
							return true;
						}
					} else {
						currentStackLevel--;
					}
				}
			}

			return false;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class MapKeyIterator<V> extends AbstractMapIterator<V> implements
					SupplierIterator<java.lang.Integer, V> {

		MapKeyIterator(AbstractMapNode<V> rootNode) {
			super(rootNode);
		}

		@Override
		public java.lang.Integer next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			} else {
				return currentValueNode.getKey(currentValueCursor++);
			}
		}

		@Override
		public V get() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class MapValueIterator<V> extends AbstractMapIterator<V> implements
					SupplierIterator<V, java.lang.Integer> {

		MapValueIterator(AbstractMapNode<V> rootNode) {
			super(rootNode);
		}

		@Override
		public V next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			} else {
				return currentValueNode.getValue(currentValueCursor++);
			}
		}

		@Override
		public java.lang.Integer get() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class MapEntryIterator<V> extends AbstractMapIterator<V> implements
					SupplierIterator<Map.Entry<java.lang.Integer, V>, java.lang.Integer> {

		MapEntryIterator(AbstractMapNode<V> rootNode) {
			super(rootNode);
		}

		@Override
		public Map.Entry<java.lang.Integer, V> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			} else {
				return currentValueNode.getKeyValueEntry(currentValueCursor++);
			}
		}

		@Override
		public java.lang.Integer get() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Iterator that first iterates over inlined-values and then continues depth
	 * first recursively.
	 */
	private static class TrieMap_5Bits_IntKeyNodeIterator<V> implements
					Iterator<AbstractMapNode<V>> {

		final Deque<Iterator<? extends AbstractMapNode<V>>> nodeIteratorStack;

		TrieMap_5Bits_IntKeyNodeIterator(AbstractMapNode<V> rootNode) {
			nodeIteratorStack = new ArrayDeque<>();
			nodeIteratorStack.push(Collections.singleton(rootNode).iterator());
		}

		@Override
		public boolean hasNext() {
			while (true) {
				if (nodeIteratorStack.isEmpty()) {
					return false;
				} else {
					if (nodeIteratorStack.peek().hasNext()) {
						return true;
					} else {
						nodeIteratorStack.pop();
						continue;
					}
				}
			}
		}

		@Override
		public AbstractMapNode<V> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			AbstractMapNode<V> innerNode = nodeIteratorStack.peek().next();

			if (innerNode.hasNodes()) {
				nodeIteratorStack.push(innerNode.nodeIterator());
			}

			return innerNode;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	static final class TransientTrieMap_5Bits_IntKey<V> extends AbstractMap<java.lang.Integer, V>
					implements TransientMap<java.lang.Integer, V> {
		final private AtomicReference<Thread> mutator;
		private AbstractMapNode<V> rootNode;
		private int hashCode;
		private int cachedSize;

		TransientTrieMap_5Bits_IntKey(TrieMap_5Bits_IntKey<V> trieMap_5Bits_IntKey) {
			this.mutator = new AtomicReference<Thread>(Thread.currentThread());
			this.rootNode = trieMap_5Bits_IntKey.rootNode;
			this.hashCode = trieMap_5Bits_IntKey.hashCode;
			this.cachedSize = trieMap_5Bits_IntKey.cachedSize;
			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
		}

		private boolean checkHashCodeAndSize(final int targetHash, final int targetSize) {
			int hash = 0;
			int size = 0;

			for (Iterator<Map.Entry<java.lang.Integer, V>> it = entryIterator(); it.hasNext();) {
				final Map.Entry<java.lang.Integer, V> entry = it.next();
				final int key = entry.getKey();
				final V val = entry.getValue();

				hash += (int) key ^ val.hashCode();
				size += 1;
			}

			return hash == targetHash && size == targetSize;
		}

		@Override
		public boolean containsKey(Object o) {
			try {
				@SuppressWarnings("unchecked")
				final int key = (int) o;
				return rootNode.containsKey(key, (int) key, 0);
			} catch (ClassCastException unused) {
				return false;
			}
		}

		@Override
		public boolean containsKeyEquivalent(Object o, Comparator<Object> cmp) {
			try {
				@SuppressWarnings("unchecked")
				final int key = (int) o;
				return rootNode.containsKey(key, (int) key, 0, cmp);
			} catch (ClassCastException unused) {
				return false;
			}
		}

		@Override
		public V get(Object o) {
			try {
				@SuppressWarnings("unchecked")
				final int key = (int) o;
				final Optional<V> result = rootNode.findByKey(key, (int) key, 0);

				if (result.isPresent()) {
					return result.get();
				} else {
					return null;
				}
			} catch (ClassCastException unused) {
				return null;
			}
		}

		@Override
		public V getEquivalent(Object o, Comparator<Object> cmp) {
			try {
				@SuppressWarnings("unchecked")
				final int key = (int) o;
				final Optional<V> result = rootNode.findByKey(key, (int) key, 0, cmp);

				if (result.isPresent()) {
					return result.get();
				} else {
					return null;
				}
			} catch (ClassCastException unused) {
				return null;
			}
		}

		@Override
		public V __put(final java.lang.Integer key, final V val) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			final int keyHash = key.hashCode();
			final Result<V> details = Result.unchanged();

			final CompactMapNode<V> newRootNode = rootNode.updated(mutator, key, val, keyHash, 0,
							details);

			if (details.isModified()) {
				rootNode = newRootNode;

				if (details.hasReplacedValue()) {
					final V old = details.getReplacedValue();

					final int valHashOld = old.hashCode();
					final int valHashNew = val.hashCode();

					hashCode += keyHash ^ valHashNew;
					hashCode -= keyHash ^ valHashOld;
					// cachedSize remains same

					if (DEBUG) {
						assert checkHashCodeAndSize(hashCode, cachedSize);
					}
					return old;
				} else {
					final int valHashNew = val.hashCode();

					hashCode += keyHash ^ valHashNew;
					cachedSize += 1;

					if (DEBUG) {
						assert checkHashCodeAndSize(hashCode, cachedSize);
					}
					return null;
				}
			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return null;
		}

		@Override
		public V __putEquivalent(final java.lang.Integer key, final V val,
						final Comparator<Object> cmp) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			final int keyHash = key.hashCode();
			final Result<V> details = Result.unchanged();

			final CompactMapNode<V> newRootNode = rootNode.updated(mutator, key, val, keyHash, 0,
							details, cmp);

			if (details.isModified()) {
				rootNode = newRootNode;

				if (details.hasReplacedValue()) {
					final V old = details.getReplacedValue();

					final int valHashOld = old.hashCode();
					final int valHashNew = val.hashCode();

					hashCode += keyHash ^ valHashNew;
					hashCode -= keyHash ^ valHashOld;
					// cachedSize remains same

					if (DEBUG) {
						assert checkHashCodeAndSize(hashCode, cachedSize);
					}
					return old;
				} else {
					final int valHashNew = val.hashCode();

					hashCode += keyHash ^ valHashNew;
					cachedSize += 1;

					if (DEBUG) {
						assert checkHashCodeAndSize(hashCode, cachedSize);
					}
					return null;
				}
			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return null;
		}

		@Override
		public boolean __putAll(final Map<? extends java.lang.Integer, ? extends V> map) {
			boolean modified = false;

			for (Entry<? extends java.lang.Integer, ? extends V> entry : map.entrySet()) {
				final boolean isPresent = containsKey(entry.getKey());
				final V replaced = __put(entry.getKey(), entry.getValue());

				if (!isPresent || replaced != null) {
					modified = true;
				}
			}

			return modified;
		}

		@Override
		public boolean __putAllEquivalent(final Map<? extends java.lang.Integer, ? extends V> map,
						final Comparator<Object> cmp) {
			boolean modified = false;

			for (Entry<? extends java.lang.Integer, ? extends V> entry : map.entrySet()) {
				final boolean isPresent = containsKeyEquivalent(entry.getKey(), cmp);
				final V replaced = __putEquivalent(entry.getKey(), entry.getValue(), cmp);

				if (!isPresent || replaced != null) {
					modified = true;
				}
			}

			return modified;
		}

		@Override
		public boolean __remove(final java.lang.Integer key) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");

			}

			final int keyHash = key.hashCode();
			final Result<V> details = Result.unchanged();

			final CompactMapNode<V> newRootNode = rootNode.removed(mutator, key, keyHash, 0,
							details);

			if (details.isModified()) {

				assert details.hasReplacedValue();
				final int valHash = details.getReplacedValue().hashCode();

				rootNode = newRootNode;
				hashCode -= keyHash ^ valHash;
				cachedSize -= 1;

				if (DEBUG) {
					assert checkHashCodeAndSize(hashCode, cachedSize);
				}
				return true;

			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return false;
		}

		@Override
		public boolean __removeEquivalent(final java.lang.Integer key, Comparator<Object> cmp) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			final int keyHash = key.hashCode();
			final Result<V> details = Result.unchanged();

			final CompactMapNode<V> newRootNode = rootNode.removed(mutator, key, keyHash, 0,
							details, cmp);

			if (details.isModified()) {

				assert details.hasReplacedValue();
				final int valHash = details.getReplacedValue().hashCode();

				rootNode = newRootNode;
				hashCode -= keyHash ^ valHash;
				cachedSize -= 1;

				if (DEBUG) {
					assert checkHashCodeAndSize(hashCode, cachedSize);
				}
				return true;

			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return false;
		}

		@Override
		public Set<java.util.Map.Entry<java.lang.Integer, V>> entrySet() {
			Set<java.util.Map.Entry<java.lang.Integer, V>> entrySet = null;

			if (entrySet == null) {
				entrySet = new AbstractSet<java.util.Map.Entry<java.lang.Integer, V>>() {
					@Override
					public Iterator<java.util.Map.Entry<java.lang.Integer, V>> iterator() {
						return new Iterator<Entry<java.lang.Integer, V>>() {
							private final Iterator<Entry<java.lang.Integer, V>> i = entryIterator();

							@Override
							public boolean hasNext() {
								return i.hasNext();
							}

							@Override
							public Entry<java.lang.Integer, V> next() {
								return i.next();
							}

							@Override
							public void remove() {
								i.remove();
							}
						};
					}

					@Override
					public int size() {
						return TransientTrieMap_5Bits_IntKey.this.size();
					}

					@Override
					public boolean isEmpty() {
						return TransientTrieMap_5Bits_IntKey.this.isEmpty();
					}

					@Override
					public void clear() {
						TransientTrieMap_5Bits_IntKey.this.clear();
					}

					@Override
					public boolean contains(Object k) {
						return TransientTrieMap_5Bits_IntKey.this.containsKey(k);
					}
				};
			}
			return entrySet;
		}

		@Override
		public int size() {
			return cachedSize;
		}

		@Override
		public SupplierIterator<java.lang.Integer, V> keyIterator() {
			return new TransientMapKeyIterator<>(this);
		}

		@Override
		public Iterator<V> valueIterator() {
			// return new TrieMapValueIterator<>(keyIterator());
			return new MapValueIterator<>(rootNode); // TODO: iterator does not
														// support removal
		}

		@Override
		public Iterator<Map.Entry<java.lang.Integer, V>> entryIterator() {
			// return new TrieMapEntryIterator<>(keyIterator());
			return new MapEntryIterator<>(rootNode); // TODO: iterator does not
														// support removal
		}

		/**
		 * Iterator that first iterates over inlined-values and then continues
		 * depth first recursively.
		 */
		private static class TransientMapKeyIterator<V> extends AbstractMapIterator<V> implements
						SupplierIterator<java.lang.Integer, V> {

			final TransientTrieMap_5Bits_IntKey<V> transientTrieMap_5Bits_IntKey;
			java.lang.Integer lastKey;

			TransientMapKeyIterator(TransientTrieMap_5Bits_IntKey<V> transientTrieMap_5Bits_IntKey) {
				super(transientTrieMap_5Bits_IntKey.rootNode);
				this.transientTrieMap_5Bits_IntKey = transientTrieMap_5Bits_IntKey;
			}

			@Override
			public java.lang.Integer next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				} else {
					lastKey = currentValueNode.getKey(currentValueCursor++);
					return lastKey;
				}
			}

			@Override
			public V get() {
				throw new UnsupportedOperationException();
			}

			/*
			 * TODO: test removal with iteration rigorously
			 */
			@Override
			public void remove() {
				boolean success = transientTrieMap_5Bits_IntKey.__remove(lastKey);

				if (!success) {
					throw new IllegalStateException("Key from iteration couldn't be deleted.");
				}
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other == null) {
				return false;
			}

			if (other instanceof TransientTrieMap_5Bits_IntKey) {
				TransientTrieMap_5Bits_IntKey<?> that = (TransientTrieMap_5Bits_IntKey<?>) other;

				if (this.size() != that.size()) {
					return false;
				}

				return rootNode.equals(that.rootNode);
			}

			return super.equals(other);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public ImmutableMap<java.lang.Integer, V> freeze() {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			mutator.set(null);
			return new TrieMap_5Bits_IntKey<V>(rootNode, hashCode, cachedSize);
		}
	}

}