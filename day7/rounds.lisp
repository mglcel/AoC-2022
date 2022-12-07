(let ((quicklisp-init (merge-pathnames "quicklisp/setup.lisp" (user-homedir-pathname))))
    (when (probe-file quicklisp-init) (load quicklisp-init))
)

(ql:quickload :cl-ppcre)

(defvar sp_req 30000000)
(defvar sp_max 70000000)
(defvar sp_min 100000)

(defvar round1 0)
(defvar round2 (list))
(defvar round2_tb3d 0)

(defun build (in root) ;; Build a tree structure of the disk
    (let ((struct ()) )
        (do ((line (read-line in nil) (read-line in nil) ))
            ((or (null line) (string= line "$ cd ..")))
                (ppcre:register-groups-bind (dir) ("\\$ cd ([^\\.].*)" line :sharedp t)
                    (push (build in dir) struct)
                )
                (ppcre:register-groups-bind () ("\\$ ls" line :sharedp t)
                    (do ( (last_pos (file-position in) (file-position in)) 
                          (nline (read-line in nil) (read-line in nil)) )
                        ( (or (null nline) (string= (char nline 0) "$"))
                          (file-position in last_pos) )
                        (ppcre:register-groups-bind (size file) ("(\\d+) (.*)" nline :sharedp t)
                            (push (list file (parse-integer size)) struct)
                        )
                    )
                )
        )
        (list root struct)
    )
)

(defun total (dir children) ;; Cumulate sizes on directories, skip files from tree
    (let ( (size 0) (nc (list)) )
        (map nil #'(lambda (e) 
               (if (integerp (first (last e)))
                    (incf size (first (last e)))
                    (let ((subtotal (total (first e) (second e))))
                          (incf size (first(last subtotal)))
                          (push subtotal nc)
                    )
               )
            ) children
        )
        (when (<= size sp_min) (incf round1 size) )
        (push (cons dir size) round2)
        (list dir nc size)
    )
)

(with-open-file (in "input.txt")
    (let ((struct (first(second(build in "."))))(dirs)) 
        ;; round 1
        (setq dirs (total (first struct) (second struct)))
        (format T "round1 = ~a~%" round1)
        
        ;; round 2
        (setq round2_tb3d (- sp_req (- sp_max (first(last dirs)))))
        (let ((selected nil))
            (mapcar 
              (lambda (x) (when (and (>(cdr x) round2_tb3d) (not selected)) (setq selected (cdr x)) ) ) 
              (sort round2 (lambda (a b) (< (cdr a) (cdr b))))
            )
            (format T "round2 = ~a~%" selected)
        )
    )
)

