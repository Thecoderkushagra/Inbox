import React, { useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { Paperclip, Image as ImageIcon, File, Loader2 } from 'lucide-react';
import { mediaApi } from '@/api';

interface MediaUploaderProps {
  onMediaUploaded: (file: File) => void;
  disabled?: boolean;
}

export const MediaUploader: React.FC<MediaUploaderProps> = ({
  onMediaUploaded,
  disabled,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      toast.error('File size exceeds 10MB limit');
      return;
    }

    onMediaUploaded(file);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div>
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        className="hidden"
        accept="image/*,video/*,audio/*,.pdf,.txt,.zip,.doc,.docx"
        disabled={disabled}
      />
      <button
        type="button"
        onClick={() => fileInputRef.current?.click()}
        disabled={disabled}
        className="p-2.5 rounded-xl text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors disabled:opacity-50"
        title="Attach file (Images, Videos, Audio, Docs up to 10MB)"
      >
        <Paperclip className="w-5 h-5" />
      </button>
    </div>
  );
};
