import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account, Api, errorMessage } from './api';

@Component({
  selector: 'app-accounts',
  imports: [FormsModule, DecimalPipe],
  template: `
    <h2>Comptes bancaires</h2>

    @if (error()) {
      <p class="alert error">{{ error() }}</p>
    }

    <table>
      <thead>
        <tr>
          <th>Référence client</th>
          <th>Identifiant</th>
          <th>Statut</th>
          <th>Devise</th>
          <th class="num">Solde</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        @for (a of accounts(); track a.id) {
          <tr>
            <td>{{ a.customerReference }}</td>
            <td class="mono">{{ a.id }}</td>
            <td>
              <span class="badge" [class.ok]="a.status === 'ACTIVE'" [class.ko]="a.status === 'BLOCKED'">{{ a.status }}</span>
            </td>
            <td>{{ a.currency }}</td>
            <td class="num">{{ a.balance | number: '1.2-2' }}</td>
            <td>
              @if (a.status === 'ACTIVE') {
                <button class="link" (click)="block(a)">Bloquer</button>
              } @else {
                <button class="link" (click)="activate(a)">Activer</button>
              }
            </td>
          </tr>
        } @empty {
          <tr><td colspan="6" class="muted">Aucun compte.</td></tr>
        }
      </tbody>
    </table>

    <h3>Créer un compte</h3>
    <form class="row" (ngSubmit)="create()">
      <input name="ref" placeholder="Référence client (ex : CUST-010)" [(ngModel)]="customerReference" required />
      <input name="cur" placeholder="Devise" maxlength="3" [(ngModel)]="currency" required />
      <input name="bal" type="number" min="0" step="0.01" placeholder="Solde initial" [(ngModel)]="initialBalance" required />
      <button type="submit">Créer</button>
    </form>
  `,
})
export class Accounts {
  private readonly api = inject(Api);

  protected readonly accounts = signal<Account[]>([]);
  protected readonly error = signal('');

  protected customerReference = '';
  protected currency = 'EUR';
  protected initialBalance = 0;

  constructor() {
    this.load();
  }

  protected load() {
    this.api.accounts().subscribe({
      next: (list) => {
        this.accounts.set(list);
        this.error.set('');
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected create() {
    this.api
      .createAccount({
        customerReference: this.customerReference,
        currency: this.currency,
        initialBalance: Number(this.initialBalance),
      })
      .subscribe({
        next: () => {
          this.customerReference = '';
          this.load();
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
  }

  protected block(a: Account) {
    this.api.blockAccount(a.id).subscribe({ next: () => this.load(), error: (e) => this.error.set(errorMessage(e)) });
  }

  protected activate(a: Account) {
    this.api.activateAccount(a.id).subscribe({ next: () => this.load(), error: (e) => this.error.set(errorMessage(e)) });
  }
}
