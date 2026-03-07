import { Link } from "@/components/link";
import { Button } from "@/components/ui/button";

export default function HomePage() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-card/50 backdrop-blur">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <Link href="/" className="font-semibold text-lg tracking-tight">
            Future Scope
          </Link>
          <nav className="flex items-center gap-4">
            <Link href="/jobs">
              <Button variant="ghost" size="sm">
                Jobs
              </Button>
            </Link>
            <Link href="/login">
              <Button variant="ghost" size="sm">
                Sign in
              </Button>
            </Link>
            <Link href="/signup">
              <Button size="sm">
                Get started
              </Button>
            </Link>
          </nav>
        </div>
      </header>
      <main className="flex-1 flex flex-col items-center justify-center px-4 py-24 text-center">
        <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl font-sans">
          Recruitment & AI Interview Platform
        </h1>
        <p className="mt-4 max-w-2xl text-lg text-muted-foreground font-sans">
          Hire with confidence. Screen candidates, run structured interviews, and
          use AI-powered insights—all in one place.
        </p>
        <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
          <Link href="/signup">
            <Button size="lg" className="rounded-lg">
              Create your company
            </Button>
          </Link>
          <Link href="/jobs">
            <Button size="lg" variant="outline" className="rounded-lg">
              Browse jobs
            </Button>
          </Link>
        </div>
      </main>
      <footer className="border-t py-6 text-center text-sm text-muted-foreground">
        Future Scope — Built for modern recruiting
      </footer>
    </div>
  );
}
