# Clean target folder
rm -rf target/generated-docs
mkdir -p target/generated-docs

# Copy documentation content in target
cp -r docs/* target/generated-docs
