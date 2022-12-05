
.arch armv8-a

// syscalls
.equ SYS_EXIT,            1                // Linux syscalls
.equ SYS_READ,            3
.equ SYS_WRITE,           4
.equ SYS_OPEN,            5
.equ SYS_CLOSE,           6

// constants
.equ STDOUT,              1
.equ O_RDONLY,            0x0000
.equ BUFFERSIZE,          100
.equ LINESIZE,            100              // limitation: maxLine = 100
.equ EOL,                 0x0A

// readfile struct
.equ readfile_Fd,         0
.equ readfile_buffer,     8*0x01
.equ readfile_buffersize, 8*0x02
.equ readfile_line,       8*0x03
.equ readfile_linesize,   8*0x04
.equ readfile_pointer,    8*0x05
.equ readfile_end,        8*0x06

// ----------------------------------------------------------------------------

// initialized data
.data
szFilename:               .asciz               "input.txt"
szErrormsg:               .asciz               "Error detected.\n"
szCarriageReturn:         .asciz               "\n"

// uninitialized data
.bss 
sBuffer:                  .skip BUFFERSIZE     // buffer result
szLineBuffer:             .skip LINESIZE       // max line size
.align 4
stReadFile:               .skip readfile_end   // structure storage

// code
.text
.globl _start

// ----------------------------------------------------------------------------

.align 4

_start:
        adrp X0, szFilename@PAGE                // filename
        add X0, X0, szFilename@PAGEOFF
        mov X1, #O_RDONLY                       // flags
        mov X2, #0                              // mode
        mov X16, #SYS_OPEN                      // open file
        svc 0x80

        cmp X0, #0                              // error ?
        ble error

        adrp X1, stReadFile@PAGE                // init structure readfile
        add X1, X1, stReadFile@PAGEOFF
        str X0, [X1, #readfile_Fd]              // save FD in structure
        adrp X0, sBuffer@PAGE                   // buffer address
        add X0, X0, sBuffer@PAGEOFF
        str X0, [X1, #readfile_buffer]
        mov X0, #BUFFERSIZE                     // buffer size
        str X0, [X1, #readfile_buffersize]
        adrp X0, szLineBuffer@PAGE              // line buffer address
        add X0, X0, szLineBuffer@PAGEOFF
        str X0, [X1, #readfile_line]
        mov X0, #LINESIZE                       // line buffer size
        str X0, [X1, #readfile_linesize]
        mov X0, #BUFFERSIZE// + 1                 // init read pointer
        str X0, [X1, #readfile_pointer]                       

1:                                              // begin read loop
        mov X0, X1                              // read one line
        bl readLineFile

        cmp X0, #0
        beq end                                 // end loop if no line read
        blt error                               // error ?

        adrp X0, szLineBuffer@PAGE              // display line
        add X0, X0, szLineBuffer@PAGEOFF
        bl print

        adrp X0, szCarriageReturn@PAGE          // display skipped line return
        add X0, X0, szCarriageReturn@PAGEOFF
        bl print

        adrp X1, stReadFile@PAGE                // X1 has been destroyed
        add X1, X1, stReadFile@PAGEOFF

        b 1b                                    // and loop

end:
        adrp X1, stReadFile@PAGE
        add X1, X1, stReadFile@PAGEOFF

        ldr X0, [X1, #readfile_Fd]              // load FD from structure
        mov X16, #SYS_CLOSE                     // call system close file
        svc 0x80 

        cmp X0, #0
        blt error                               // error ?

        mov X0, #0                              // return code 0
        b 100f                                  // branch end

error:
	// TODO: better manage error here
        mov X0, #1                              // return error code 1
100:                                            // standard end of the program
        mov X16, #SYS_EXIT                      // request to exit program
        svc 0x80 

// ----------------------------------------------------------------------------

readLineFile:
    mov X9, LR                                  // save LR
    mov X4, X0                                  // load structure

    ldr X1, [X4, #readfile_buffer]
    ldr X2, [X4, #readfile_buffersize]

    ldr X5, [X4, #readfile_pointer]
    ldr X6, [X4, #readfile_linesize]
    ldr X7, [X4, #readfile_buffersize]
    ldr X8, [X4, #readfile_line]

    mov X3, #0                                  // init pointer line
    strb W3, [X8, X3]                           // and store 0 in line buffer

    cmp X5, X2                                  // pointer buffer < buffer size ?
    blt 2f                                      // do not read file, buffer is not empty

1:                                              // loop read file
    ldr X0, [X4, #readfile_Fd]
    mov X16, #SYS_READ                          // read file
    mov X10, X1                                 // save X1
    svc 0x80
    mov X1, X10                                 // restore X1

    cmp X0, #0                                  // error read or end -> end loop
    ble 100f

    mov X7, X0                                  // number of read characters (set buffersize)
    mov X5, #0                                  // init buffer pointer

2:                                              // begin loop copy characters
    ldrb W0, [X1, X5]                           // load 1 character (0-extended) read buffer
    cmp X0, #EOL                                // end of line ?
    beq 4f

    strb W0, [X8,X3]                            // store character in line buffer
    add X3, X3, #1                              // increment pointer line

    cmp X3, X6
    bgt 1000f                                   // line buffer too small -> error

    add X5, X5, #1                              // increment buffer pointer
    cmp X5, X2                                  // end buffer ?
    bge 1b                                      // yes new read

    cmp X5, X7                                  // read characters ?
    blt 2b                                      // loop

    cmp X3, #0                                  // no more characters in line buffer ?
    beq 100f                                    // return

4:
    mov X0, #0
    strb W0, [X8, X3]                           // store final string zero

    add X5, X5, #1                              // increment buffer pointer
    str X5, [X4, #readfile_pointer]             // store pointer in structure
    str X7, [X4, #readfile_buffersize]          // store number of last characters read as buffer size
    mov X0, X3                                  // return length of line

100:
    mov LR, X9                                  // restore registers
    br LR                                       // return

1000:
    mov X0, #-2
    b 100b

// ----------------------------------------------------------------------------

print:
    mov X3, LR // push {X0,X1,X2,X7, LR}        // save registers 
    mov X2, #0                                  // init counter length

1:
    ldrb W1, [X0, X2]                           // read octet start position + index 
    cmp X1, #0                                  // if 0 its over (EOS)
    bne 2f                                      // else add 1 in the length and loop

    mov X1, X0                                  // move message addr in X1 
    mov X0, #STDOUT                             // first arg to write is stdout
                                                // X2 is length of message
    mov X16, #SYS_WRITE                         // call WRITE
    svc 0x80

    mov LR, X3                                  // restore LR
    br LR                                       // return

2:
    add X2, X2, #1
    b 1b
