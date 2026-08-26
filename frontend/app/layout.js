import "../styles/globals.css";

export const metadata = {
  title: "SIH Smart Procurement",
  description: "Farmer procurement scheduling and queue management prototype"
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
