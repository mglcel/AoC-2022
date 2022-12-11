use std::collections::VecDeque;
use itertools::Itertools;
use std::fs;

type Op = Box<(dyn Fn(u16) -> u16 + 'static)>;

struct Monkey {
    items: VecDeque<u16>,
    apply: Option<Op>,
    test: Option<Box<(dyn Fn(u16) -> bool + 'static)>>,
    throw_on_true: usize,
    throw_on_false: usize,
    inspected: usize
}
impl Default for Monkey {
    fn default () -> Monkey {
        Monkey{items: VecDeque::new(), apply: None, test: None, throw_on_true: 0, throw_on_false: 0, inspected: 0}
    }
}

// ----------------------------------------------------------------------------

fn self_multiply_op() -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x * x)) }
fn self_sub_op() -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x - x)) }
fn self_sum_op() -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x + x)) }
fn self_divide_op() -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x / x)) }

fn multiply_op(v: u16) -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x * v.clone())) }
fn sub_op(v: u16) -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x - v.clone())) }
fn sum_op(v: u16) -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x + v.clone())) }
fn divide_op(v: u16) -> Box<dyn Fn(u16) -> u16> { Box::new(move |x: u16| (x / v.clone())) }

fn is_divisible(v: u16) -> Box<dyn Fn(u16) -> bool> { Box::new(move |x: u16| (x % v.clone() == 0)) }

fn new_op(op: String, value: String) -> Option<Box<dyn Fn(u16) -> u16>> {
    if value == "old" {
        match op.as_str() {
            "*" => Some(self_multiply_op()),
            "-" => Some(self_sub_op()),
            "+" => Some(self_sum_op()),
            "/" => Some(self_divide_op()),
            _ => None
        }
    } else {
        match op.as_str() {
            "*" => Some(multiply_op(value.parse::<u16>().unwrap())),
            "-" => Some(sub_op(value.parse::<u16>().unwrap())),
            "+" => Some(sum_op(value.parse::<u16>().unwrap())),
            "/" => Some(divide_op(value.parse::<u16>().unwrap())),
            _ => None
        }
    }
}

// ----------------------------------------------------------------------------

fn look_at_monkeys(monkeys: &mut Vec<Monkey>, rounds: usize) {
    for _r in 0..rounds {
        for i in 0..monkeys.len() {
            for item in monkeys[i].items.clone().iter() {
                let new_item = { monkeys[i].apply.as_ref().unwrap()(*item) / 3 };
                let throw_on = if monkeys[i].test.as_ref().unwrap()(new_item) {
                    monkeys[i].throw_on_true
                } else {
                    monkeys[i].throw_on_false
                };
                monkeys[throw_on].items.push_back(new_item);
            };
            monkeys[i].inspected += monkeys[i].items.len();
            monkeys[i].items.truncate(0);
        }
    }
}

fn monkey_business_level(monkeys: &mut Vec<Monkey>) -> usize {
    monkeys.into_iter()
        .sorted_by_key(|monkey| -(monkey.inspected as i16))
        .take(2).map(|x| x.inspected)
        .product::<usize>()
}

fn main()-> std::io::Result<()> {
    let mut monkeys : Vec<Monkey> = vec![];

    let file = fs::read_to_string("input.txt").unwrap();
    let mut i_line = 0;

    let mut c_monkey = Monkey::default();
    for line in file.lines() {
        match i_line % 7 {
            1 => c_monkey.items = VecDeque::from_iter(
                line.get(18..).unwrap().split(", ")
                    .map(|s| s.parse::<u16>().unwrap()).collect::<Vec<u16>>()),
            2 => {
                let mut params = line.get(23..).unwrap().split(" ");
                c_monkey.apply = new_op(params.next().unwrap().to_string(),
                                        params.next().unwrap().to_string());
            },
            3 => c_monkey.test =
                Some(is_divisible(line.get(21..).unwrap().parse::<u16>().unwrap())),
            4 => c_monkey.throw_on_true =
                line.get(29..).unwrap().parse::<usize>().unwrap(),
            5 => c_monkey.throw_on_false =
                line.get(30..).unwrap().parse::<usize>().unwrap(),
            6 => { monkeys.push(c_monkey) ; c_monkey = Monkey::default() },
            _ => {}
        }
        i_line += 1;
    }
    monkeys.push(c_monkey);

    look_at_monkeys(&mut monkeys, 20);
    { println!("Round 1 : {:?}", monkey_business_level(&mut monkeys)); }

    Ok(())
}
