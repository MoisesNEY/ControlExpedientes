import { useEffect } from 'react';
import { useLanguage } from '../context/LanguageContext';
import { englishTranslations } from './translations';

const textNodeOriginals = new WeakMap<Text, string>();
const elementAttributeOriginals = new WeakMap<Element, Map<string, string>>();
const translatableAttributes = ['placeholder', 'title', 'aria-label'];
const ignoredTags = new Set(['SCRIPT', 'STYLE', 'CODE', 'PRE', 'TEXTAREA']);

const normalize = (value: string) => value.replace(/\s+/g, ' ').trim();
const phraseTranslationEntries = Object.entries(englishTranslations)
  .filter(([source]) => source.length > 10 && /[\s.,:;¿?¡!]/.test(source))
  .sort(([left], [right]) => right.length - left.length);

const translate = (value: string) => {
  const normalized = normalize(value);
  if (!normalized) return null;
  const exactTranslation = englishTranslations[normalized];
  if (exactTranslation) return exactTranslation;

  let translated = normalized;
  for (const [source, replacement] of phraseTranslationEntries) {
    if (translated.includes(source)) {
      translated = translated.split(source).join(replacement);
    }
  }

  return translated === normalized ? null : translated;
};

const isIgnored = (node: Node) => {
  const element = node.nodeType === Node.ELEMENT_NODE ? (node as Element) : node.parentElement;
  return Boolean(element?.closest(Array.from(ignoredTags).join(',')) || element?.closest('[contenteditable="true"]'));
};

const translateTextNode = (node: Text, language: 'es' | 'en') => {
  if (isIgnored(node)) return;

  const current = node.nodeValue ?? '';
  if (!current.trim()) return;

  if (language === 'es') {
    const original = textNodeOriginals.get(node);
    if (original !== undefined && current !== original) {
      node.nodeValue = original;
    }
    return;
  }

  const existingOriginal = textNodeOriginals.get(node);
  if (existingOriginal !== undefined) {
    const existingTranslation = translate(existingOriginal);
    if (existingTranslation && current === existingTranslation) return;
    const updatedTranslation = translate(current);
    if (updatedTranslation) {
      textNodeOriginals.set(node, current);
      node.nodeValue = current.replace(normalize(current), updatedTranslation);
    }
    return;
  }

  const translated = translate(current);
  if (!translated) return;
  textNodeOriginals.set(node, current);
  node.nodeValue = current.replace(normalize(current), translated);
};

const translateElementAttributes = (element: Element, language: 'es' | 'en') => {
  if (isIgnored(element)) return;

  for (const attribute of translatableAttributes) {
    const current = element.getAttribute(attribute);
    if (!current) continue;

    if (language === 'es') {
      const original = elementAttributeOriginals.get(element)?.get(attribute);
      if (original && current !== original) {
        element.setAttribute(attribute, original);
      }
      continue;
    }

    const stored = elementAttributeOriginals.get(element)?.get(attribute);
    if (stored) {
      const existingTranslation = translate(stored);
      if (existingTranslation && current === existingTranslation) continue;
    }

    const translated = translate(current);
    if (!translated) continue;

    const originals = elementAttributeOriginals.get(element) ?? new Map<string, string>();
    if (!elementAttributeOriginals.has(element)) {
      elementAttributeOriginals.set(element, originals);
    }
    originals.set(attribute, current);
    element.setAttribute(attribute, translated);
  }
};

const walkAndTranslate = (root: Node, language: 'es' | 'en') => {
  if (root.nodeType === Node.TEXT_NODE) {
    translateTextNode(root as Text, language);
    return;
  }

  if (root.nodeType !== Node.ELEMENT_NODE && root.nodeType !== Node.DOCUMENT_NODE) return;

  if (root.nodeType === Node.ELEMENT_NODE) {
    translateElementAttributes(root as Element, language);
  }

  const walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT);
  let node = walker.nextNode();
  while (node) {
    if (node.nodeType === Node.TEXT_NODE) {
      translateTextNode(node as Text, language);
    } else if (node.nodeType === Node.ELEMENT_NODE) {
      translateElementAttributes(node as Element, language);
    }
    node = walker.nextNode();
  }
};

export const GlobalTranslator = () => {
  const { language } = useLanguage();

  useEffect(() => {
    const root = document.getElementById('root');
    if (!root) return undefined;

    walkAndTranslate(root, language);

    const observer = new MutationObserver(mutations => {
      for (const mutation of mutations) {
        if (mutation.type === 'characterData') {
          walkAndTranslate(mutation.target, language);
          continue;
        }
        if (mutation.type === 'attributes') {
          walkAndTranslate(mutation.target, language);
          continue;
        }
        mutation.addedNodes.forEach(node => walkAndTranslate(node, language));
      }
    });

    observer.observe(root, {
      childList: true,
      subtree: true,
      characterData: true,
      attributes: true,
      attributeFilter: translatableAttributes,
    });

    return () => observer.disconnect();
  }, [language]);

  return null;
};
