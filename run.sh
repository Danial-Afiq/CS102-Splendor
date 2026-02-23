if [ ! -d "out" ]; then
  echo "out/ not found. Compiling first"
  ./compile.sh
fi

java -cp out splendor.app.Main