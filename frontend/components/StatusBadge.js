const tones = {
  WAITING: "muted",
  CALLED: "warning",
  ARRIVED: "info",
  VERIFICATION: "info",
  PROCUREMENT: "strong",
  COMPLETED: "success",
  PENDING: "warning",
  PROCESSING: "info",
  PAID: "success",
  SKIPPED: "muted",
  CANCELLED: "danger"
};

export default function StatusBadge({ value }) {
  return <span className={`badge ${tones[value] || "muted"}`}>{String(value || "NA").replace("_", " ")}</span>;
}
