package com.xebialabs.overthere.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

public class ReplacingInputStream extends FilterInputStream {
    LinkedList<Integer> inQueue = new LinkedList<Integer>();
    LinkedList<Integer> outQueue = new LinkedList<Integer>();
    final byte[] search, replacement;

    protected ReplacingInputStream(InputStream in, String elevatedPassword) {
        super(in);
        this.search = elevatedPassword.getBytes();
        this.replacement = "***".getBytes();
    }

    private boolean isMatchFound() {
        Iterator<Integer> inIter = inQueue.iterator();
        for (int i = 0; i < search.length; i++)
            if (!inIter.hasNext() || search[i] != inIter.next())
                return false;
        return true;
    }

    private void readAhead() throws IOException {
        // Work up some look-ahead.
        while (inQueue.size() < search.length) {
            int next = super.read();
            inQueue.offer(next);
            if (next == -1)
                break;
        }
    }

    @Override
    public int read() throws IOException {
        // Next byte already determined.
        if (outQueue.isEmpty()) {
            readAhead();

            if (isMatchFound()) {
                for (int i = 0; i < search.length; i++)
                    inQueue.remove();

                for (byte b : replacement)
                    outQueue.offer((int) b);
            } else
                outQueue.add(inQueue.remove());
        }

        return outQueue.remove();
    }
    // TODO: Override the other read methods.
}
