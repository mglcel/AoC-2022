#!/opt/homebrew/bin/python3

# Build P(riority) = { 'a': 1, ..., 'z': 25, 'A': 26, ..., 'Z': 52 }
P = [{ chr(x):x-ord(A)+N for x in range(ord(A), ord(A) + 26) } for A,N in { 'a':1, 'A':27 }.items()]
P = P[0] | P[1]

total1, total2, group = 0, 0, []

file = open("input.txt", "r")
for line in file:
  l = len(line)
  total1 += P[( set(line[:l//2]) & set(line[l//2:].strip()) ).pop()]

  group.append(line.strip())
  if len(group) == 3:
    total2 += P[( set(group[0]) & set(group[1]) & set(group[2]) ).pop()]
    group = []

file.close()
print(f"total round 1: {total1}")
print(f"total round 2: {total2}")
