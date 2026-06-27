import Link from "next/link";
import { ExternalLink, Film, ShieldCheck } from "lucide-react";
import { PageTitle, Panel } from "../../components/ui";
import { TmdbLogo } from "../../components/tmdb-logo";

export default function AboutPage() {
  return (
    <>
      <PageTitle
        title="About CineStack"
        subtitle="A non-commercial portfolio application for organizing and discussing movies."
      />

      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <section className="space-y-6">
          <Panel>
            <div className="flex items-start gap-3">
              <Film className="mt-0.5 text-accent" size={22} />
              <div>
                <h2 className="text-lg font-semibold">The project</h2>
                <p className="mt-2 text-sm leading-6 text-neutral-700">
                  CineStack lets users create public or private watchlists, track viewing status,
                  rate and review movies, and discover titles through movie search. It is an
                  educational portfolio project and does not charge users or display advertising.
                </p>
              </div>
            </div>
          </Panel>

          <Panel>
            <div className="flex items-start gap-3">
              <ShieldCheck className="mt-0.5 text-accent" size={22} />
              <div>
                <h2 className="text-lg font-semibold">Data and independence</h2>
                <p className="mt-2 text-sm leading-6 text-neutral-700">
                  Account details, watchlists, ratings, reviews, and notes are CineStack user data.
                  Movie metadata retrieved from TMDB is cached temporarily and refreshed or removed
                  according to the configured retention policy. CineStack is independently developed.
                </p>
              </div>
            </div>
          </Panel>
        </section>

        <Panel>
          <div className="space-y-4">
            <div className="flex min-h-20 items-center justify-center rounded border border-dashed border-line bg-panel p-4">
              <TmdbLogo />
            </div>
            <div>
              <h2 className="text-lg font-semibold">TMDB attribution</h2>
              <p className="mt-2 text-sm leading-6 text-neutral-700">
                This application uses TMDB and the TMDB APIs but is not endorsed, certified, or
                otherwise approved by TMDB.
              </p>
            </div>
            <div className="grid gap-2 text-sm">
              <a
                className="inline-flex items-center gap-2 font-medium text-accent"
                href="https://www.themoviedb.org/about/logos-attribution"
                rel="noreferrer"
                target="_blank"
              >
                TMDB logos and attribution <ExternalLink size={14} />
              </a>
              <a
                className="inline-flex items-center gap-2 font-medium text-accent"
                href="https://www.themoviedb.org/api-terms-of-use"
                rel="noreferrer"
                target="_blank"
              >
                TMDB API Terms of Use <ExternalLink size={14} />
              </a>
              <Link className="font-medium text-accent" href="/dashboard">Return to dashboard</Link>
            </div>
          </div>
        </Panel>
      </div>
    </>
  );
}
