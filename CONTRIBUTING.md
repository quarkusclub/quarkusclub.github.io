# Thank you for your interest in contributing to the **Quarkus Club**! 🎉

This document is a set of guidelines to help you contribute to this project. Feel free to suggest changes to this document in a Pull Request.

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
    - [Reporting Bugs & Suggesting Improvements](#reporting-bugs--suggesting-improvements)
    - [Creating a Blog Post](#creating-a-blog-post)
    - [Your Pull Request](#your-pull-request)
3. [Development Guide](#development-guide)
    - [PreRequisites](#prerequisites)
    - [Environment Setup](#environment-setup)
    - [Project Structure](#project-structure)
    - [Running the Project](#running-the-project)
4. [Style Guide](#style-guide)
    - [Commit Messages](#commit-messages)

---

## Code of Conduct

This project and all those who participate in it are governed by the [Code of Conduct](https://www.contributor-covenant.org/version/3/0/code_of_conduct/). By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.


## How Can I Contribute?

### Reporting Bugs & Suggesting Improvements

Bugs and suggestions are tracked as [GitHub Issues](https://github.com/quarkusclub/quarkusclub.github.io/issues). When creating an issue:

- Use a clear and descriptive title.
- Describe the exact steps to reproduce the problem (for bugs).
- Explain why this improvement would be useful (for suggestions).

### How Creating a Blog Post ?

Create an issue for this And To add a new post to the blog, follow these steps:

1.  **Create a folder** in `content/posts/` using the format `YYYY-MM-DD-slug` (e.g., `2025-02-24-my-new-post`).
2.  **Create an `index.md` file** inside that folder.
3.  **Add Frontmatter** at the top of the file:
    ```markdown
    ---
    title: "My New Post Title"
    description: "A short description of the post."
    image: cover.jpg
    layout: post
    tags: [quarkus, java]
    author: your-github-username
    ---
    ```
4.  **Add your content** in Markdown below the frontmatter.
5.  If you have images, place them inside the same folder and reference them in the frontmatter or content.

### Your Pull Request

1.  **Fork** the repository and clone it locally.
2.  **Create a branch** for your edit: `git checkout -b post/my-new-post` or `feat/some-improvement`.
3.  **Make your changes** and commit: `git commit -m 'Adds new blog post about X'`.
4.  **Push** to your fork: `git push origin my-branch-name`.
5.  **Create a Pull Request** against the `main` branch of the original repository.

## Development Guide

This project is a static blog generated with **Quarkus Roq**.

### PreRequisites

To run this project, you will need:

-   **JDK 21** or higher.
-   **Maven** (optional, as the project includes the `mvnw` wrapper).

### Environment Setup

Clone the repository:

```bash
git clone https://github.com/your-github-profile/quarkusclub.github.io
cd quarkusclub.github.io
```

### Project Structure

-   `content/`: Contains the static content (posts, events, and pages).
    -   `posts/`: Each folder here is a blog post.
    -   `events/`: Event collections.
-   `public/`: Static assets like icons or global images.
-   `src/main/resources/application.properties`: Site configuration and collection settings.
-   `templates/`: Qute templates used to render the site.

### Running the Project

To start Quarkus's development mode (dev mode), which allows live reload:

**Linux/macOS:**
```bash
./mvnw quarkus:dev
```

**Windows:**
```cmd
mvnw.cmd quarkus:dev
```

The site will be available at `http://localhost:8080`.

## Style Guide

### Commit Messages

-   Use the imperative mode in the subject ("Add feature" and not "Adding feature").
-   The first line must have a maximum of 50 characters.
-   Reference issues and pull requests liberally after the first line.
