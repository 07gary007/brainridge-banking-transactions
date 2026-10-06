package com.brainridge.banking.transaction;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final ConcurrentMap<UUID, CopyOnWriteArrayList<Transaction>> transactionsByAccount =
            new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        transactionsByAccount
                .computeIfAbsent(transaction.accountId(), ignored -> new CopyOnWriteArrayList<>())
                .add(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> findByAccountId(UUID accountId) {
        return transactionsByAccount.getOrDefault(accountId, new CopyOnWriteArrayList<>())
                .stream()
                .sorted(Comparator.comparing(Transaction::createdAt).reversed())
                .toList();
    }
}
