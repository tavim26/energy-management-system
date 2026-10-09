import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { LoaderCircle, MessageCircleQuestion, SendHorizontal, X } from 'lucide-react';
import { IconButton } from '@/components/ui/Button';
import { cn } from '@/lib/cn';
import { getErrorMessage } from '@/lib/errors';
import { supportApi, type AnswerSource } from './api';

interface ChatMessage {
  id: number;
  author: 'user' | 'assistant';
  text: string;
  source?: AnswerSource;
  failed?: boolean;
}

const SUGGESTIONS = ['How is my hourly limit set?', 'When do I get an alert?', 'How do I get a new device?'];

// Floating help button that opens the support chat
export function SupportChat() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const nextId = useRef(0);
  const listRef = useRef<HTMLDivElement>(null);

  const send = useMutation({ mutationFn: supportApi.sendMessage });

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight });
  }, [messages, send.isPending]);

  function addMessage(message: Omit<ChatMessage, 'id'>) {
    nextId.current += 1;
    setMessages((current) => [...current, { ...message, id: nextId.current }]);
  }

  async function ask(text: string) {
    const question = text.trim();

    if (!question || send.isPending) {
      return;
    }

    addMessage({ author: 'user', text: question });
    setDraft('');

    try {
      const answer = await send.mutateAsync(question);
      addMessage({ author: 'assistant', text: answer.response, source: answer.source });
    } catch (err) {
      addMessage({ author: 'assistant', text: getErrorMessage(err, 'The message could not be sent.'), failed: true });
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void ask(draft);
  }

  return (
    <div className="fixed bottom-4 right-4 z-40 flex flex-col items-end gap-3 sm:bottom-6 sm:right-6">
      {open && (
        <section
          aria-label="Support chat"
          className="flex h-[min(32rem,calc(100vh-7rem))] w-[min(22rem,calc(100vw-2rem))] flex-col overflow-hidden rounded-lg border border-line bg-surface shadow-xl"
        >
          <header className="flex items-center justify-between border-b border-line px-4 py-3">
            <div>
              <h2 className="font-semibold">Support</h2>
              <p className="text-xs text-muted">Questions about your devices, consumption and alerts</p>
            </div>
            <IconButton label="Close support chat" onClick={() => setOpen(false)}>
              <X className="size-4" aria-hidden />
            </IconButton>
          </header>

          <div ref={listRef} className="flex-1 space-y-3 overflow-y-auto px-4 py-4" aria-live="polite">
            {messages.length === 0 ? (
              <div>
                <p className="text-sm text-muted">Ask a question, or start with one of these:</p>
                <div className="mt-3 flex flex-col items-start gap-2">
                  {SUGGESTIONS.map((suggestion) => (
                    <button
                      key={suggestion}
                      type="button"
                      onClick={() => void ask(suggestion)}
                      className="rounded-md border border-line px-3 py-1.5 text-left text-sm hover:border-brand hover:text-brand"
                    >
                      {suggestion}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              messages.map((message) => (
                <div key={message.id} className={cn('flex', message.author === 'user' ? 'justify-end' : 'justify-start')}>
                  <div
                    className={cn(
                      'max-w-[85%] rounded-lg px-3 py-2 text-sm',
                      message.author === 'user' && 'bg-brand text-white',
                      message.author === 'assistant' && !message.failed && 'bg-paper',
                      message.failed && 'bg-danger-soft text-danger',
                    )}
                  >
                    <p className="whitespace-pre-line">{message.text}</p>
                    {message.source === 'AI' && <p className="mt-1 text-xs text-muted">Answered by the AI assistant</p>}
                  </div>
                </div>
              ))
            )}

            {send.isPending && (
              <div className="flex items-center gap-2 text-sm text-muted" role="status">
                <LoaderCircle className="size-4 animate-spin" aria-hidden />
                Writing an answer
              </div>
            )}
          </div>

          <form onSubmit={handleSubmit} className="flex gap-2 border-t border-line p-3">
            <label htmlFor="support-message" className="sr-only">
              Your question
            </label>
            <input
              id="support-message"
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              placeholder="Type your question"
              maxLength={1000}
              autoComplete="off"
              className="h-10 flex-1 rounded-md border border-line px-3 text-sm focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20"
            />
            <button
              type="submit"
              aria-label="Send"
              disabled={!draft.trim() || send.isPending}
              className="flex size-10 items-center justify-center rounded-md bg-brand text-white hover:bg-brand-strong disabled:opacity-50"
            >
              <SendHorizontal className="size-4" aria-hidden />
            </button>
          </form>
        </section>
      )}

      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        aria-expanded={open}
        className="flex h-12 items-center gap-2 rounded-full bg-ink px-5 text-sm font-medium text-white shadow-lg hover:bg-brand-strong"
      >
        <MessageCircleQuestion className="size-5" aria-hidden />
        {open ? 'Close help' : 'Help'}
      </button>
    </div>
  );
}