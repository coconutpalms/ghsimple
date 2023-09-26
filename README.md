# ghsimple - The fastest way to launch a static website. 

 1. Create a github repo containing index.html and any other files (images, css, js, fonts) that make up the website.
 2. Purchase your desired domain name from DNSimple.
 3. Run the script.

It is slightly more complicated than that because you must for example obtain a computer, write the website, generate access tokens for DNSimple and Github, but if you want to create a bunch of web pages fast this will do it for you.

# GitHub Pages and DNSimple Automation Script

This repository contains a Babashka Clojure script designed to automate the process of setting up a domain with GitHub Pages and DNSimple. The script configures a GitHub repository for GitHub Pages, adds a CNAME file to the repository, retrieves the DNSimple account ID, gets the domain ID from DNSimple, and applies the GitHub Pages service to the domain.

## Prerequisites

- [Babashka](https://babashka.org/)
- A GitHub account and a repository to set up with GitHub Pages.
- A DNSimple account and a domain to link with GitHub Pages.

## Environment Variables

Before running the script, you need to set up the following environment variables:

- `GITHUB_TOKEN`: Your GitHub API token.
- `DNSIMPLE_TOKEN`: Your DNSimple API token.

You can set them in your shell profile file or export them in your terminal session:

```sh
export GITHUB_TOKEN=your_github_token
export DNSIMPLE_TOKEN=your_dnsimple_token
```

## Usage

To use the script, you need to pass the following command-line arguments:

- `--gh-branch`: The GitHub branch to be used for GitHub Pages.
- `--gh-username`: Your GitHub username.
- `--gh-repo-name`: The name of your GitHub repository.
- `--domain-name`: The domain name to be linked with GitHub Pages.

Here’s how you can run the script with the required arguments:

```sh
bb ghsimple-dns.clj --gh-branch main --gh-username your_username --gh-repo-name your_repo_name --domain-name sunshinetechnologyprograms.com
```

### Example

For instance, to set up the domain `sunshinetechnologyprograms.com` for a repository named `example-repo` owned by the user `john_doe` using the `main` branch, you would run:

```sh
bb ghsimple-dns.clj --gh-branch main --gh-username john_doe --gh-repo-name example_repo --domain-name sunshinetechnologyprograms.com
```

## Example Site

[sunshinetechnologyprograms.com](http://sunshinetechnologyprograms.com/) is an example of a site set up using this script. It serves as the online presence for Sunshine Technology Programs, a software firm specializing in delivering high-quality websites.
## Contributing

If you have suggestions for improving this script or encounter any issues, feel free to open an issue or submit a pull request.

## License

This project is open source and available under the [MIT License](LICENSE).

## Acknowledgements

Special thanks to Sunshine Technology Programs for providing an example and for their contributions to the open-source community.
