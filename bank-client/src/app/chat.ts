import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api, errorMessage } from './api';

interface ChatMessage {
  from: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-chat',
  imports: [FormsModule],
  template: `
    <h2>Assistant bancaire</h2>
    <p class="muted">Exemples : « Quel est le solde de CUST-001 ? », « Vire 100 EUR de CUST-001 vers CUST-002 ».</p>

    <div class="chat">
      @for (m of messages(); track $index) {
        <div class="bubble" [class.user]="m.from === 'user'" [class.bot]="m.from === 'bot'">{{ m.text }}</div>
      }
      @if (loading()) {
        <div class="bubble bot muted">L'assistant réfléchit…</div>
      }
    </div>

    <form class="row" (ngSubmit)="send()">
      <input name="msg" class="grow" placeholder="Posez votre question…" [(ngModel)]="input" autocomplete="off" />
      <button type="submit" [disabled]="loading()">Envoyer</button>
    </form>
  `,
})
export class Chat {
  private readonly api = inject(Api);

  protected readonly messages = signal<ChatMessage[]>([]);
  protected readonly loading = signal(false);
  protected input = '';
  private conversationId: string | null = null;

  protected send() {
    const text = this.input.trim();
    if (!text || this.loading()) {
      return;
    }
    this.input = '';
    this.messages.update((list) => [...list, { from: 'user', text }]);
    this.loading.set(true);
    this.api.chat(text, this.conversationId).subscribe({
      next: (r) => {
        this.conversationId = r.conversationId;
        this.messages.update((list) => [...list, { from: 'bot', text: r.reply }]);
        this.loading.set(false);
      },
      error: (e) => {
        this.messages.update((list) => [...list, { from: 'bot', text: errorMessage(e) }]);
        this.loading.set(false);
      },
    });
  }
}
