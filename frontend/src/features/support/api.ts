import { http } from '@/lib/http';

export type AnswerSource = 'RULE' | 'AI' | 'FALLBACK';

export interface ChatAnswer {
  response: string;
  source: AnswerSource;
}

export const supportApi = {
  async sendMessage(message: string): Promise<ChatAnswer> {
    const { data } = await http.post<ChatAnswer>('/api/support/message', { message });
    return data;
  },
};