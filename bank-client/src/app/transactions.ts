import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account, Api, Transaction, errorMessage } from './api';

@Component({
  selector: 'app-transactions',
  imports: [FormsModule, DatePipe, DecimalPipe],
  template: `
    <h2>Transactions</h2>

    <h3>Nouvelle transaction</h3>
    <form class="row" (ngSubmit)="create()">
      <select name="acc" [(ngModel)]="accountId" (ngModelChange)="onAccountChange($event)" required>
        <option value="" disabled>Compte à débiter</option>
        @for (a of accounts(); track a.id) {
          <option [value]="a.id">{{ a.customerReference }} · {{ a.currency }} · {{ a.balance | number: '1.2-2' }}</option>
        }
      </select>
      <input name="amount" type="number" min="0.01" step="0.01" placeholder="Montant" [(ngModel)]="amount" required />
      <input name="cur" placeholder="Devise" maxlength="3" [(ngModel)]="currency" required />
      <select name="cat" [(ngModel)]="merchantCategory">
        <option>GROCERY</option>
        <option>TRAVEL</option>
        <option>ELECTRONICS</option>
        <option>RESTAURANT</option>
        <option>OTHER</option>
      </select>
      <input name="country" placeholder="Pays" maxlength="2" [(ngModel)]="country" />
      <button type="submit">Enregistrer</button>
    </form>

    @if (message()) {
      <p class="alert success">{{ message() }}</p>
    }
    @if (error()) {
      <p class="alert error">{{ error() }}</p>
    }

    <h3>Historique</h3>
    <table>
      <thead>
        <tr>
          <th>Date</th>
          <th>Compte</th>
          <th class="num">Montant</th>
          <th>Devise</th>
          <th>Catégorie</th>
          <th>Pays</th>
        </tr>
      </thead>
      <tbody>
        @for (t of transactions(); track t.id) {
          <tr>
            <td>{{ t.createdAt | date: 'dd/MM/yyyy HH:mm:ss' }}</td>
            <td class="mono">{{ t.accountId.substring(0, 8) }}…</td>
            <td class="num">{{ t.amount | number: '1.2-2' }}</td>
            <td>{{ t.currency }}</td>
            <td>{{ t.merchantCategory }}</td>
            <td>{{ t.country }}</td>
          </tr>
        } @empty {
          <tr><td colspan="6" class="muted">Aucune transaction.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class Transactions {
  private readonly api = inject(Api);

  protected readonly accounts = signal<Account[]>([]);
  protected readonly transactions = signal<Transaction[]>([]);
  protected readonly message = signal('');
  protected readonly error = signal('');

  protected accountId = '';
  protected amount: number | null = null;
  protected currency = 'EUR';
  protected merchantCategory = 'GROCERY';
  protected country = 'FR';

  constructor() {
    this.api.accounts().subscribe({
      next: (list) => this.accounts.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.loadTransactions();
  }

  protected onAccountChange(id: string) {
    const account = this.accounts().find((a) => a.id === id);
    if (account) {
      this.currency = account.currency;
    }
  }

  protected loadTransactions() {
    this.api.transactions().subscribe({
      next: (list) => this.transactions.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected create() {
    this.message.set('');
    this.error.set('');
    this.api
      .createTransaction({
        accountId: this.accountId,
        amount: Number(this.amount),
        currency: this.currency,
        merchantCategory: this.merchantCategory,
        country: this.country,
      })
      .subscribe({
        next: (t) => {
          this.message.set(`Transaction ${t.id} enregistrée.`);
          this.amount = null;
          this.loadTransactions();
          this.api.accounts().subscribe((list) => this.accounts.set(list));
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
