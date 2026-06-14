"use client";

import { useEffect, useState } from "react";
import { api, Review } from "../../lib/api";
import { PageTitle, Panel } from "../../components/ui";

export default function ReviewsPage() {
  const [reviews, setReviews] = useState<Review[]>([]);

  useEffect(() => {
    api<Review[]>("/api/me/reviews").then(setReviews).catch(() => window.location.href = "/login");
  }, []);

  return (
    <>
      <PageTitle title="My Reviews" subtitle="Your movie reviews across public and private visibility." />
      <div className="grid gap-3">
        {reviews.map(review => (
          <Panel key={review.id}>
            <h2 className="font-medium">{review.movie.title} · {review.rating}/5 · {review.visibility}</h2>
            <p className="text-sm text-neutral-700">{review.body}</p>
          </Panel>
        ))}
      </div>
    </>
  );
}

