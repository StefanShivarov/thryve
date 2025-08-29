import React from "react";

type BaseProps = React.HTMLAttributes<HTMLButtonElement> & { asChild?: boolean };
export function Button({ className = "", ...props }: BaseProps) {
    return (
        <button
            className={
                "inline-flex items-center justify-center rounded-lg border px-4 py-2 text-sm font-medium " +
                "transition-colors shadow-sm hover:bg-gray-50 active:translate-y-[1px] " +
                className
            }
            {...props}
        />
    );
}

type InputProps = React.InputHTMLAttributes<HTMLInputElement>;
export function Input({ className = "", ...props }: InputProps) {
    return (
        <input
            className={
                "w-full rounded-lg border px-3 py-2 text-sm outline-none " +
                "focus:ring-2 focus:ring-black/10 " + className
            }
            {...props}
        />
    );
}

type SelectProps = React.SelectHTMLAttributes<HTMLSelectElement>;
export function Select({ className = "", children, ...props }: SelectProps) {
    return (
        <select
            className={
                "w-full rounded-lg border px-3 py-2 text-sm outline-none " +
                "focus:ring-2 focus:ring-black/10 " + className
            }
            {...props}
        >
            {children}
        </select>
    );
}
