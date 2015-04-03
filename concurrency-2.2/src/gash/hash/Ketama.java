package gash.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * See memcache-code/SockIOPool.java
 * 
 * 
 * MemCached Java client, connection pool for Socket IO Copyright (c) 2005
 * 
 * This module is Copyright (c) 2005 Greg Whalin, Richard Russo All rights
 * reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * @author greg whalin <greg@meetup.com>
 * @author Richard 'toast' Russo <russor@msoe.edu>
 * 
 * @version 1.3.2
 */
public class Ketama implements HashAlgo {
	private MessageDigest md5 = null; // avoid recurring construction

	public void init(Properties notused) {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("no md5 algorythm found");
		}
	}

	/**
	 * Calculates the ketama hash value for a string (originally: md5HashingAlg)
	 * 
	 * @param s
	 * @return
	 */
	public Long hash(String key) {

		if (md5 == null)
			init(null);

		md5.reset();
		md5.update(key.getBytes());
		byte[] bKey = md5.digest();
		long res = ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) | ((long) (bKey[1] & 0xFF) << 8)
				| (long) (bKey[0] & 0xFF);
		return res;
	}
}
