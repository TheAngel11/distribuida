import random


def generate_transactions(num_lines=50):
    transactions = []

    for _ in range(num_lines):
        # Choose the type of transaction
        b_type = random.choice(["b<0>", "b<1>", "b<2>", "b"])
        transaction = [b_type]

        # Determine the number of operations (between 2 and 6)
        num_operations = random.randint(2, 6)

        # If b_type is "b", include both read and write operations
        if b_type == "b":
            for _ in range(num_operations):
                operation_type = random.choice(["r", "w"])
                if operation_type == "r":
                    transaction.append(f"r({random.randint(1, 100)})")
                else:
                    transaction.append(f"w({random.randint(1, 100)},{random.randint(1, 100)})")
        else:
            # If b_type is "b<x>", include only read operations
            for _ in range(num_operations):
                transaction.append(f"r({random.randint(1, 100)})")

        # Add commit operation at the end
        transaction.append("c")

        # Join the transaction components into a single string
        transactions.append(", ".join(transaction))

    return transactions


# Generate 50 lines of transactions
sample_transactions = generate_transactions()

# Writing the transactions to a file
file_path = 'transactions.txt'

with open(file_path, 'w') as file:
    for transaction in sample_transactions:
        file.write(transaction + '\n')
