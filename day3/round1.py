#!/opt/homebrew/bin/python3

# Build P(riority) = { 'a': 1, ..., 'z': 25, 'A': 26, ..., 'Z': 52 }
P = [{ chr(x):x-ord(A)+N for x in range(ord(A), ord(A) + 26) } for A,N in { 'a':1, 'A':27 }.items() ]
P = P[0] | P[1]

total = 0
cumul = lambda N: lambda x: Occ.update( {x: Occ.get(x, N) + N} )

file = open("input.txt", "r")
for line in file:
  l, Occ = len(line), dict()

  # Cumulate item types by compartments taking advantage of decimal system
  [ cumul(1)(x) for x in list(line[:l//2]) ]
  [ cumul(100)(x) for x in list(line[l//2:].strip()) ]

  # Filter common item types, one and only one should exists
  common = dict(filter(lambda x: ( x[1] > 100 and x[1] % 100 > 0 ), Occ.items()))
  assert len(common) == 1, "none or multiple common items detected"

  # Add item type by priority
  total += P[next(iter(common.keys()))]

print(total)
