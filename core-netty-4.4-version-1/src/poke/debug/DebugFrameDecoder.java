/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.debug;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class DebugFrameDecoder extends LengthFieldBasedFrameDecoder {
	public DebugFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
			int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

		Object rtn = null;
		try {
			rtn = super.decode(ctx, in);

			System.err.println("----------------------------");
			int readerIndex = in.readerIndex();
			System.err.println("reader index: " + readerIndex);
			if (rtn != null) {
				byte[] arr = ((ByteBuf) rtn).array();
				if (arr != null) {
					for (byte b : arr)
						System.err.println(b);
				} else
					System.err.println("buffer is empty");
			}
			System.err.println("----------------------------");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rtn;
	}
}
