#!/opt/homebrew/bin/perl

use strict;

my $round = 1; # exercice round : 1 or 2

my $total = 0;
my %play_index = ('A', 0, 'B', 1, 'C', 2, 'X', 0, 'Y', 1, 'Z', 2);
my @plays = ( [4, 8, 3], [1, 5, 9], [7, 2, 6] );

@plays = map { [ sort { $a <=> $b } @{$_} ] } @plays if ($round == 2);

open(INPUT, 'input.txt') or die "file error";
while ( my @play = split(' ', <INPUT>) )  {
    $total += $plays[$play_index{$play[0]}][$play_index{$play[1]}];
}
close(INPUT);

print "$total\n";
