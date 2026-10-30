import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account, Api, errorMessage } from './api';

@Component({
  selector: 'app-transfer',
  imports: [FormsModule, DecimalPipe],
  template: `
    <h2>Virement</h2>

    <form class="column" (ngSubmit)="submit()">
      <label>
        Compte source
        <select name="from" [(ngModel)]="fromAccountId" (ngModelChange)="onFromChange($event)" required>
          <option value="" disabled>Choisir…</option>
          @for (a of accounts(); track a.id) {
            <option [value]="a.id">{{ a.customerReference }} · {{ a.currency }} · {{ a.balance | number: '1.2-2' }}</option>
          }
        </select>
      </label>
      <label>
        Compte destination
        <select name="to" [(ngModel)]="toAccountId" required>
          <option value="" disabled>Choisir…</option>
          @for (a of accounts(); track a.id) {
            <option [value]="a.id">{{ a.customerReference }} · {{ a.currency }}</option>
          }
        </select>
      </label>
      <label>
        Montant
        <input name="amount" type="number" min="0.01" step="0.01" [(ngModel)]="amount" required />
      </label>
      <label>
        Devise
        <input name="cur" maxlength="3" [(ngModel)]="currency" required />
      </label>
      <button type="submit">Effectuer le virement</button>
    </form>

    @if (message()) {
      <p class="alert success">{{ message() }}</p>
    }
    @if (error()) {
      <p class="alert error">{{ error() }}</p>
    }
  `,
})
export class Transfer {
  private readonly api = inject(Api);

  protected readonly accounts = signal<Account[]>([]);
  protected readonly message = signal('');
  protected readonly error = signal('');

  protected fromAccountId = '';
  protected toAccountId = '';
  protected amount: number | null = null;
  protected currency = 'EUR';

  constructor() {
    this.loadAccounts();
  }

  private loadAccounts() {
    this.api.accounts().subscribe({
      next: (list) => this.accounts.set(list),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected onFromChange(id: string) {
    const account = this.accounts().find((a) => a.id === id);
    if (account) {
      this.currency = account.currency;
    }
  }

  protected submit() {
    this.message.set('');
    this.error.set('');
    this.api
      .transfer({
        fromAccountId: this.fromAccountId,
        toAccountId: this.toAccountId,
        amount: Number(this.amount),
        currency: this.currency,
      })
      .subscribe({
        next: (r) => {
          this.message.set(`Virement effectué de ${r.debit.amount} ${r.debit.currency}.`);
          this.amount = null;
          this.loadAccounts();
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }
}
