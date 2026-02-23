rm -rf out
mkdir -p out

javac -d out \
  splendor/entities/*.java \
  splendor/app/Main.java

echo "Compiled to ./out"