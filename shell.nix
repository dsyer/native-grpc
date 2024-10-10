with import <nixpkgs> { };

mkShell {
  name = "env";
  buildInputs = [ graalvm-ce ];
}
