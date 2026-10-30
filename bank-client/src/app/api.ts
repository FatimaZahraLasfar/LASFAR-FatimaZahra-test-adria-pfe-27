import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

export interface Account {
  id: string;
  customerReference: string;
  status: 'ACTIVE' | 'BLOCKED';
  currency: string;
  balance: number;
}

export interface Transaction {
  id: string;
  accountId: string;
  amount: number;
  currency: string;
  merchantCategory: string | null;
  country: string | null;
  createdAt: string;
}

export interface TransferResult {
  debit: Transaction;
  credit: Transaction;
}

export interface ChatReply {
  conversationId: string;
  reply: string;
}

/** Extrait le message lisible d'une erreur (ProblemDetail renvoyé par les services). */
export function errorMessage(e: unknown): string {
  if (e instanceof HttpErrorResponse) {
    if (e.status === 0) {
      return 'Impossible de joindre la gateway (http://localhost:8080). Les services sont-ils démarrés ?';
    }
    return e.error?.detail ?? e.error?.message ?? `Erreur ${e.status}`;
  }
  return 'Erreur inattendue';
}

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);

  accounts() {
    return this.http.get<Account[]>('/api/accounts');
  }

  createAccount(body: { customerReference: string; currency: string; initialBalance: number }) {
    return this.http.post<Account>('/api/accounts', body);
  }

  blockAccount(id: string) {
    return this.http.post<Account>(`/api/accounts/${id}/block`, {});
  }

  activateAccount(id: string) {
    return this.http.post<Account>(`/api/accounts/${id}/activate`, {});
  }

  transactions(accountId?: string) {
    const url = accountId ? `/api/transactions?accountId=${accountId}` : '/api/transactions';
    return this.http.get<Transaction[]>(url);
  }

  createTransaction(body: {
    accountId: string;
    amount: number;
    currency: string;
    merchantCategory: string;
    country: string;
  }) {
    return this.http.post<Transaction>('/api/transactions', body);
  }

  transfer(body: { fromAccountId: string; toAccountId: string; amount: number; currency: string }) {
    return this.http.post<TransferResult>('/api/transfers', body);
  }

  chat(message: string, conversationId: string | null) {
    return this.http.post<ChatReply>('/api/chat', { message, conversationId });
  }
}
