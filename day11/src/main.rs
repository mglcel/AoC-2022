use std::collections::VecDeque;
use itertools::Itertools;
use std::fs;

struct Monkey {
    items: VecDeque<u64>,
    apply: Option<Box<(dyn Fn(u64) -> u64 + 'static)>>,
    test: Option<Box<(dyn Fn(u64) -> bool + 'static)>>,
    throw_on_true: usize,
    throw_on_false: usize,
    inspected: u64
}
impl Default for Monkey {
    fn default () -> Monkey {
        Monkey{items: VecDeque::new(), apply: None, test: None,
            throw_on_true: 0, throw_on_false: 0, inspected: 0}
    }
}

// ----------------------------------------------------------------------------

fn self_multiply_op() -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x * x)) }
fn self_sub_op() -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x - x)) }
fn self_sum_op() -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x + x)) }
fn self_divide_op() -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x / x)) }

fn multiply_op(v: u64) -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x * v.clone())) }
fn sub_op(v: u64) -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x - v.clone())) }
fn sum_op(v: u64) -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x + v.clone())) }
fn divide_op(v: u64) -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x / v.clone())) }

fn mod_op(v: u64) -> Box<dyn Fn(u64) -> u64> { Box::new(move |x: u64| (x % v.clone())) }
fn is_divisible(v: u64) -> Box<dyn Fn(u64) -> bool> { Box::new(move |x: u64| (x % v.clone() == 0)) }

fn new_op(op: String, value: String) -> Option<Box<dyn Fn(u64) -> u64>> {
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
            "*" => Some(multiply_op(value.parse::<u64>().unwrap())),
            "-" => Some(sub_op(value.parse::<u64>().unwrap())),
            "+" => Some(sum_op(value.parse::<u64>().unwrap())),
            "/" => Some(divide_op(value.parse::<u64>().unwrap())),
            _ => None
        }
    }
}

// ----------------------------------------------------------------------------

fn look_at_monkeys(monkeys: &mut Vec<Monkey>, apply_div: Box<dyn Fn(u64) -> u64>, rounds: i64) {
    for _r in 0..rounds {
        for i in 0..monkeys.len() {
            for item in monkeys[i].items.clone().iter() {
                let new_item:u64 = { apply_div(monkeys[i].apply.as_ref().unwrap()(*item)) };
                let throw_on = if monkeys[i].test.as_ref().unwrap()(new_item) {
                    monkeys[i].throw_on_true
                } else {
                    monkeys[i].throw_on_false
                };
                monkeys[throw_on].items.push_back(new_item);
            };
            monkeys[i].inspected += monkeys[i].items.len() as u64;
            monkeys[i].items.truncate(0);
        }
    }
}

fn monkey_business_level(monkeys: &mut Vec<Monkey>) -> u64 {
    monkeys.into_iter()
        .sorted_by_key(|monkey| -(monkey.inspected as i64))
        .take(2).map(|x| x.inspected)
        .product::<u64>()
}

fn main() {
    let round = 2; // Set round to 1 or 2 : TODO: pass as arg

    let mut monkeys : Vec<Monkey> = vec![];
    let file = fs::read_to_string("input.txt").unwrap();

    let mut c_monkey = Monkey::default();
    let mut div_product = 1u64;
    let mut i_line = 0;
    for line in file.lines() {
        match i_line % 7 {
            1 => c_monkey.items = VecDeque::from_iter(
                line.get(18..).unwrap().split(", ")
                    .map(|s| s.parse::<u64>().unwrap()).collect::<Vec<u64>>()
            ),
            2 => {
                let mut params = line.get(23..).unwrap().split(" ");
                c_monkey.apply = new_op(params.next().unwrap().to_string(),
                                        params.next().unwrap().to_string());
            },
            3 => {
                let div = line.get(21..).unwrap().parse::<u64>().unwrap();
                div_product *= div;
                c_monkey.test = Some(is_divisible(div))
            },
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

    if round == 1 {
        look_at_monkeys(&mut monkeys, divide_op(3), 20);
        { println!("Round 1 : {:?}", monkey_business_level(&mut monkeys)); }
    } else {
        look_at_monkeys(&mut monkeys, mod_op(div_product), 10_000);
        { println!("Round 2 : {:?}", monkey_business_level(&mut monkeys)); }
    }
}
