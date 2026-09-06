import { describe, it, expect } from 'vitest';
import { XSS_ATTACK_VECTORS } from '../../fixtures/message_fixtures';

/**
 * Unit Test: Input Sanitization & Anti-XSS Guards
 * Verifies that HTML tags, script execution payloads, and javascript: links are neutralised.
 */
describe('Backend Unit: Input Sanitizer & @NoHtml Rules', () => {
  // Regex mimicking Spring Boot NoHtmlValidator implementation
  const HTML_TAG_PATTERN = /<[^>]*>|&#\d+;|&[a-z]+;/i;
  const JAVASCRIPT_PROTOCOL_PATTERN = /javascript\s*:/i;
  const DATA_PROTOCOL_PATTERN = /data\s*:\s*text\/html/i;

  function validateNoHtml(value: string | null | undefined): boolean {
    if (!value) return true;
    if (HTML_TAG_PATTERN.test(value)) return false;
    if (JAVASCRIPT_PROTOCOL_PATTERN.test(value)) return false;
    if (DATA_PROTOCOL_PATTERN.test(value)) return false;
    return true;
  }

  function sanitizeForDisplay(input: string): string {
    return input
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  describe('@NoHtml Annotation Behavior on Titles and Names', () => {
    it('accepts clean plain-text titles', () => {
      expect(validateNoHtml('Engineering Standup Room')).toBe(true);
      expect(validateNoHtml('Project Alpha [Sprint 4]')).toBe(true);
    });

    it('detects and flags all raw XSS vectors in input fields', () => {
      for (const vector of XSS_ATTACK_VECTORS) {
        const isValid = validateNoHtml(vector);
        expect(isValid, `Expected vector to be flagged as invalid HTML: ${vector}`).toBe(false);
      }
    });
  });

  describe('HTML Entity Encoding for Safe Rendering', () => {
    it('escapes dangerous HTML characters to prevent DOM execution', () => {
      const dangerousScript = '<script>alert("xss")</script>';
      const sanitized = sanitizeForDisplay(dangerousScript);

      expect(sanitized).not.toContain('<script>');
      expect(sanitized).toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
    });

    it('escapes image onerror payloads', () => {
      const imgPayload = '<img src="x" onerror="evil()" />';
      const sanitized = sanitizeForDisplay(imgPayload);

      expect(sanitized).not.toContain('<img');
      expect(sanitized).toContain('&lt;img');
      expect(sanitized).toContain('&gt;');
    });
  });
});
