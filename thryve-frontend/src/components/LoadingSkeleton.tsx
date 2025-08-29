export default function LoadingSkeleton() {
    return (
        <div className="animate-pulse rounded-2xl border p-4">
            <div className="h-32 w-full rounded-xl bg-gray-200" />
            <div className="mt-3 h-4 w-2/3 rounded bg-gray-200" />
            <div className="mt-2 h-3 w-5/6 rounded bg-gray-200" />
            <div className="mt-2 h-3 w-3/5 rounded bg-gray-200" />
            <div className="mt-4 h-8 w-24 rounded-lg bg-gray-200" />
        </div>
    );
}
