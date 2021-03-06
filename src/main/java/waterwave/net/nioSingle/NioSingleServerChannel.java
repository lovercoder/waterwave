package waterwave.net.nioSingle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import shui.common.buffer.BufferPoolSingleNIO;
import shui.common.service.ThreadSharedService;
import waterwave.net.nioSingle.define.NioSingleServerDataDealer;

public class NioSingleServerChannel extends ThreadSharedService {

	SocketChannel sc;

	BufferPoolSingleNIO bp;

	NioSingleServerDataDealer dealer;

	boolean isClosed = false;

	public NioSingleServerChannel(SocketChannel sc, BufferPoolSingleNIO bp, NioSingleServerDataDealer dealer) {
		super();
		this.sc = sc;
		this.bp = bp;
		this.dealer = dealer;
	}

	public int read() {
		ByteBuffer b = bp.getBufferNio();
		int r = read(b);
		dealer.serverOnData(this, b, r);
		return r;
	}

	public int read(ByteBuffer b) {
		// ByteBuffer b = bp.allocate();
		// ByteBuffer b = bp.getBuffer();

		int in = -1;
		try {
			in = read0(b);
			//log.log(1, "-------->sc read:", in);
			if (in < 0) {
				//log.log(1, "-------->sc !!:", in);
				bp.giveupBuffer(b);
				close();
				dealer.serverOnClose(this);
			} else if (in == 0) {
				//log.log(1, "-------->sc!!!:", in);
			}

			// dealer.serverOnData(this, b, in);

		} catch (IOException e) {
			bp.giveupBuffer(b);
			e.printStackTrace();
			close();
		}
		return in;
	}
	//
	// public void read(ByteBuffer b, int in) {
	// dealer.serverOnData(this, b, in);
	// }

	public void close() {
		close0();
		dealer.serverOnClose(this);

	}

	public void close0() {
		this.isClosed = true;
		try {
			log.log(2, "server channel close ing", sc);
			sc.close();
		} catch (IOException e) {
			//
			e.printStackTrace();
		}
	}

	public void write() {
		// TODO Auto-generated method stub

	}

	public int read0(ByteBuffer b) throws IOException {
		int r = sc.read(b);
		return r;
	}

	public int write0(ByteBuffer b) throws IOException {
		b.flip();
		int w = sc.write(b);
		if (b.hasRemaining()) {
			log.log(2, "b.hasRemaining()", b.remaining());
			while (b.hasRemaining()) {
				write0(b);
			}
		}
		//log.log(1, "s write0", w);
		bp.giveupBuffer(b);
		// TODO
		// dealer.clientAfterWrite(this, b, w);
		return w;
	}

	public SocketChannel getChannel() {
		return sc;
	}

	public void setChannel(SocketChannel channel) {
		this.sc = channel;
	}

	public NioSingleServerDataDealer getDealer() {
		return dealer;
	}

	public void setDealer(NioSingleServerDataDealer dealer) {
		this.dealer = dealer;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
}
