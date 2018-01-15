package de.tuberlin.inet.sdwn.core.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SdwnTransactionChain {

    private final List<SdwnTransaction> transactions = new ArrayList<>();
    private Iterator<SdwnTransaction> iterator;

    public SdwnTransactionChain(SdwnTransaction t) {
        transactions.add(t);
    }

    public SdwnTransactionChain append(SdwnTransaction t) {
        transactions.add(t);
        return this;
    }

    public SdwnTransaction next() {
        if (iterator == null) {
            iterator = transactions.listIterator();
        }

        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
   }
}
