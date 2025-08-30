import { useState } from "react";
import SectionResources from "./SectionResources";

type Section = { id: string | number; title: string; textContent?: string; orderNumber: number };

export default function SectionCard({
                                        section,
                                        canManage,
                                        onEdit,
                                        onDelete,
                                    }: {
    section: Section;
    canManage: boolean;
    onEdit: () => void;
    onDelete: () => void;
}) {
    const [open, setOpen] = useState(false);

    return (
        <li className="overflow-hidden rounded-2xl border bg-white shadow-sm">
            {/* Header row */}
            <div
                className="flex cursor-pointer items-start justify-between p-4 hover:bg-gray-50"
                onClick={() => setOpen((v) => !v)}
            >
                <div>
                    <div className="text-sm text-gray-500">Lesson {section.orderNumber}</div>
                    <div className="mt-1 text-base font-semibold">{section.title}</div>
                </div>
                <div className="flex items-center gap-2">
                    {canManage && (
                        <>
                            <button
                                className="rounded-lg border bg-white px-3 py-1.5 text-sm hover:bg-gray-50"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onEdit();
                                }}
                            >
                                Edit
                            </button>
                            <button
                                className="rounded-lg border bg-white px-3 py-1.5 text-sm hover:bg-gray-50"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onDelete();
                                }}
                            >
                                Delete
                            </button>
                        </>
                    )}
                    <span className="ml-2 text-gray-400">{open ? "▴" : "▾"}</span>
                </div>
            </div>

            {/* Body */}
            {open && (
                <>
                    {section.textContent && (
                        <div className="border-t px-4 py-3 text-sm text-gray-700 whitespace-pre-wrap">{section.textContent}</div>
                    )}
                    <SectionResources sectionId={section.id} canManage={canManage} />
                </>
            )}
        </li>
    );
}
