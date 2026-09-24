import type { ButtonHTMLAttributes } from 'react';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  rounded?: 'full' | 'default';
};

export function Button({ rounded, className = "", children, ...props }: ButtonProps) {
  const roundedClass = rounded === "full" ? "rounded-full" : "rounded-lg";

  return (
      <button
          className={`bg-orange-500 text-white px-4 py-2 ${roundedClass} hover:bg-orange-600 font-medium transition-colors ${className}`}
          {...props}
      >
        {children}
      </button>
  )
}
