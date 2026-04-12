import "@fontsource/atkinson-hyperlegible";
import hljs from 'highlight.js';
import 'highlight.js/styles/tokyo-night-dark.min.css';

hljs.highlightAll();

const btn = document.getElementById("theme-toggle");

btn.addEventListener("click", () => {
  document.body.classList.toggle("dark");
  document.body.classList.toggle("light");
});
