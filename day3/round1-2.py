#!/opt/homebrew/bin/python3

# Build P(riority) = { 'a': 1, ..., 'z': 25, 'A': 26, ..., 'Z': 52 }
P = [{ chr(x):x-ord(A)+N for x in range(ord(A), ord(A) + 26) } for A,N in { 'a':1, 'A':27 }.items() ]
P = P[0] | P[1]

total = 0

file = open("input.txt", "r")
for line in file:
  l = len(line)
  total += P[next(iter(set(line[:l//2]).intersection(list(line[l//2:].strip()))))]

print(total)
