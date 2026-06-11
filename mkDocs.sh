# Clean target folder
rm -rf target/generated-docs
mkdir -p target/generated-docs

# npm install -g @mermaid-js/mermaid-cli
# mmdc -i docs/subscriptions-mermaid.md  -o docs/subscriptions.md

# Copy documentation content in target
cp -r docs/* target/generated-docs
