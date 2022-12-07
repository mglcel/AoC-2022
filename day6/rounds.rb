
$f = File.read("input.txt").split

def compute(s)
  ($f[0].length - (s-1)).times { |i|
    begin 
      puts i+s; break 
    end if $f[0][i,s].split('').group_by{|c|c}.values.sort_by{|v|-v.length}[0].length == 1
  }
end

compute 4
compute 14
