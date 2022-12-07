
brew install sbcl

curl -O https://beta.quicklisp.org/quicklisp.lisp

sbcl --load quicklisp.lisp --script install.lisp
