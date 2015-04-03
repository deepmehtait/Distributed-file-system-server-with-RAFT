package gash.hash;

import java.util.Properties;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * taken from:
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This is a very fast, non-cryptographic hash suitable for general hash-based
 * lookup. See http://murmurhash.googlepages.com/ for more details.
 * 
 * <p>
 * The C version of MurmurHash 2.0 found at that site was ported to Java by
 * Andrzej Bialecki (ab at getopt org).
 * </p>
 * 
 * Look at Google guava library for a better (newer murmur at 32 and 128 bit)
 */
public class MurmurHash128 implements HashAlgo {
	public static final String sConfSeed = "seed";

	Properties conf;
	int seed = 1;

	public void init(Properties conf) {
		this.conf = conf;

		if (conf != null) {
			String s = conf.getProperty(sConfSeed);
			seed = (s == null) ? 1 : Integer.parseInt(s);
		}
	}

	public Long hash(String value) {
		HashFunction hf = Hashing.murmur3_128(seed);
		HashCode hc = hf.newHasher().putString(value, Charsets.UTF_8).hash();
		return new Long(hc.asLong());
	}
}