import { Routes } from '@angular/router';
import { Accounts } from './accounts';
import { Transactions } from './transactions';
import { Transfer } from './transfer';
import { Chat } from './chat';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'accounts' },
  { path: 'accounts', component: Accounts },
  { path: 'transactions', component: Transactions },
  { path: 'transfer', component: Transfer },
  { path: 'chat', component: Chat },
];
