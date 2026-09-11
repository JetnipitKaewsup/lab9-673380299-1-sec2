# Deposit System

ระบบฝากเงินเข้าบัญชี พัฒนาด้วย **Spring Boot** และใช้ **Spring Data JPA** สำหรับจัดการข้อมูลใน Database โดยระบบประกอบด้วย `Account` และ `DepositTransaction` และเน้นการศึกษาเรื่อง **Database Transaction** ด้วย `@Transactional`

ระบบรองรับการฝากเงิน โดยเมื่อมีการฝากเงิน ระบบจะเพิ่มยอดเงินใน `Account` และสร้างรายการ `DepositTransaction` เพื่อบันทึกประวัติการฝากเงิน

จุดสำคัญของโปรเจกต์นี้คือการศึกษา **Transaction Management** เพื่อให้การเปลี่ยนแปลงข้อมูลหลายขั้นตอนสามารถทำงานร่วมกันได้อย่างถูกต้อง และสามารถ Rollback เมื่อเกิดข้อผิดพลาด

---
# รายงาน Lab 9
[รายงาน Lab 9 673380299-1 sec.2](https://github.com/JetnipitKaewsup/lab9-673380299-1-sec2/blob/main/%E0%B8%A3%E0%B8%B2%E0%B8%A2%E0%B8%87%E0%B8%B2%E0%B8%99%20Lab%209_673380299-1.pdf)

---

## 1. เทคโนโลยีที่ใช้

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Postman

---

## 2. โครงสร้างโปรเจกต์

```text
src
└── main
    └── java
        └── com.example.lab9
            ├── controller
            │   └── AccountController.java
            │
            ├── service
            │   ├── AccountService.java
            │   └── DepositService.java
            │
            ├── repository
            │   ├── AccountRepository.java
            │   └── DepositRepository.java
            │
            └── model
                ├── Account.java
                └── DepositTransaction.java
```

### หน้าที่ของแต่ละ Layer

| Layer | หน้าที่ |
|---|---|
| Model | เก็บโครงสร้างข้อมูล Account และ DepositTransaction |
| Repository | ติดต่อและจัดการข้อมูลใน Database |
| Service | จัดการ Business Logic และ Transaction |
| Controller | รับ HTTP Request และส่ง Response กลับไปยัง Client |

---

## 3. ระบบฝากเงิน

ระบบมี Endpoint สำหรับฝากเงิน:

```http
POST /accounts/{id}/deposit
```

ตัวอย่าง:

```http
POST http://localhost:8080/accounts/1/deposit
```

Request Body:

```json
{
    "amount": 1000
}
```

เมื่อฝากเงิน ระบบจะทำงานตามลำดับดังนี้

```text
Client / Postman
       ↓
AccountController
       ↓
DepositService
       ↓
ค้นหา Account
       ↓
เพิ่ม balance
       ↓
บันทึก Account
       ↓
สร้าง DepositTransaction
       ↓
บันทึก DepositTransaction
       ↓
COMMIT
```

---

## 4. Account และ DepositTransaction

ระบบมี Entity หลัก 2 ตัว

### Account

ใช้เก็บข้อมูลบัญชี เช่น

- id
- accountNumber
- balance

### DepositTransaction

ใช้เก็บประวัติการฝากเงิน เช่น

- id
- amount
- account_id

ความสัมพันธ์ระหว่าง Entity:

```text
Account 1 ───────── * DepositTransaction
```

หมายความว่า Account หนึ่งบัญชีสามารถมีรายการฝากเงินได้หลายรายการ และ DepositTransaction แต่ละรายการเป็นของ Account เพียงหนึ่งบัญชี

ตัวอย่าง:

```text
Account ID: 1
Balance: 7000

        │
        ├── DepositTransaction
        │   Amount: 1000
        │
        ├── DepositTransaction
        │   Amount: 500
        │
        └── DepositTransaction
            Amount: 1500
```

---

## 5. DepositService

เมธอดหลักสำหรับฝากเงินคือ

```java
@Transactional
public void deposit(long accountId, double amount) {

    // find account
    Account account = accountRepository.findById(accountId)
            .orElseThrow(() ->
                new RuntimeException("Account not found " + accountId));

    // update balance
    double new_balance = account.getBalance() + amount;
    account.setBalance(new_balance);
    accountRepository.save(account);

    // save transaction
    DepositTransaction deposit =
            new DepositTransaction(amount, account);
    depositRepository.save(deposit);
}
```

การทำงานของเมธอดประกอบด้วย 3 ขั้นตอนหลัก

1. ค้นหา Account จาก `accountId`
2. เพิ่มจำนวนเงินเข้า `balance` และบันทึก Account
3. สร้าง `DepositTransaction` และบันทึกลง Database

---

## 6. `@Transactional`

`@Transactional` เป็น Annotation ของ Spring ที่ใช้กำหนดขอบเขตของ Transaction

ตัวอย่าง:

```java
@Transactional
public void deposit(long accountId, double amount) {
    ...
}
```

หมายความว่าการทำงานภายในเมธอด `deposit()` จะถูกจัดการให้อยู่ภายใต้ Transaction เดียวกัน

ในระบบฝากเงิน การทำงานที่เกี่ยวข้องกับ Database ได้แก่

```text
save Account
       +
save DepositTransaction
```

ทั้งสองการทำงานควรสำเร็จพร้อมกัน

หากทุกขั้นตอนสำเร็จ:

```text
Transaction
     ↓
COMMIT
     ↓
บันทึกข้อมูลถาวรใน Database
```

แต่หากเกิดข้อผิดพลาด:

```text
Transaction
     ↓
Error
     ↓
ROLLBACK
     ↓
ยกเลิกการเปลี่ยนแปลงทั้งหมด
```

---

## 7. เหตุผลที่การฝากเงินควรใช้ Transaction

การฝากเงินไม่ได้เปลี่ยนแปลงข้อมูลเพียงจุดเดียว แต่มีการเปลี่ยนแปลงอย่างน้อย 2 ส่วน

```text
1. Account.balance
2. DepositTransaction
```

ตัวอย่างการฝาก 1,000 บาท:

```text
Account.balance
5000 → 6000

DepositTransaction
สร้างรายการ amount = 1000
```

ข้อมูลทั้งสองส่วนควรสอดคล้องกัน

หาก Account ถูกเพิ่มเงินเป็น 6,000 บาท แต่ไม่มี DepositTransaction บันทึกไว้ จะทำให้ข้อมูลของระบบไม่สอดคล้องกัน

ดังนั้นจึงควรใช้ Transaction เพื่อให้เกิดหลักการ

> **All or Nothing**

คือทำสำเร็จทั้งหมด หรือยกเลิกทั้งหมด

---

## 8. COMMIT

**COMMIT** คือการยืนยันการเปลี่ยนแปลงใน Transaction และบันทึกข้อมูลลง Database อย่างถาวร

ตัวอย่าง:

```text
เริ่ม Transaction
       ↓
balance 5000 → 6000
       ↓
save Account
       ↓
save DepositTransaction
       ↓
ทำงานสำเร็จ
       ↓
COMMIT
```

ผลลัพธ์:

```text
Account.balance = 6000

DepositTransaction
amount = 1000
```

ข้อมูลทั้งหมดถูกบันทึกลง Database

---

## 9. ROLLBACK

**ROLLBACK** คือการยกเลิกการเปลี่ยนแปลงที่เกิดขึ้นภายใน Transaction เมื่อเกิดข้อผิดพลาด

ตัวอย่างการทดลอง:

```java
@Transactional
public void deposit(long accountId, double amount) {

    Account account = accountRepository.findById(accountId)
            .orElseThrow(() ->
                new RuntimeException("Account not found " + accountId));

    double new_balance = account.getBalance() + amount;
    account.setBalance(new_balance);
    accountRepository.save(account);

    DepositTransaction deposit =
            new DepositTransaction(amount, account);
    depositRepository.save(deposit);

    throw new RuntimeException("Test Rollback");
}
```

ในกรณีนี้ Error เกิดหลังจากบันทึก Account และ DepositTransaction แล้ว

ลำดับการทำงาน:

```text
balance 5000 → 6000
       ↓
save Account
       ↓
save DepositTransaction
       ↓
RuntimeException
       ↓
ROLLBACK
```

ผลลัพธ์:

```text
Account.balance
5000
```

และ

```text
DepositTransaction
ไม่มีรายการใหม่
```

แม้ว่าจะมีการเรียก `save()` ไปแล้ว แต่ข้อมูลยังอยู่ภายใน Transaction และยังสามารถถูก Rollback ได้

---

## 10. ทดลองเปรียบเทียบมีและไม่มี `@Transactional`

### กรณีที่ 1: มี `@Transactional`

```java
@Transactional
public void deposit(long accountId, double amount) {
    ...
    accountRepository.save(account);
    ...
    depositRepository.save(deposit);

    throw new RuntimeException("Test Rollback");
}
```

ลำดับ:

```text
เริ่ม Transaction
       ↓
save Account
       ↓
save DepositTransaction
       ↓
RuntimeException
       ↓
ROLLBACK
```

ผลลัพธ์:

```text
Account.balance
กลับเป็นค่าเดิม

DepositTransaction
ไม่มีรายการใหม่
```

---

### กรณีที่ 2: ไม่มี `@Transactional`

เมื่อเอา `@Transactional` ออก:

```java
public void deposit(long accountId, double amount) {
    ...
    accountRepository.save(account);
    ...
    depositRepository.save(deposit);

    throw new RuntimeException("Test Rollback");
}
```

ลำดับ:

```text
save Account
       ↓
บันทึก Account
       ↓
save DepositTransaction
       ↓
บันทึก DepositTransaction
       ↓
RuntimeException
       ↓
เกิด Error
```

เนื่องจากไม่มี Transaction ที่ครอบการทำงานทั้งหมด การเปลี่ยนแปลงที่ถูก Commit ไปแล้วอาจยังคงอยู่ใน Database

ผลลัพธ์ที่คาดว่าจะพบ:

```text
Account.balance
เพิ่มขึ้นแล้ว

DepositTransaction
มีรายการใหม่
```

แม้ Postman จะแสดง:

```text
500 Internal Server Error
```

---

## 11. ตารางเปรียบเทียบ Transaction

สมมติข้อมูลเริ่มต้น:

```text
Account.balance = 5000
Deposit amount = 1000
```

| รายการ | มี `@Transactional` | ไม่มี `@Transactional` |
|---|---|---|
| เพิ่ม balance | 5000 → 6000 | 5000 → 6000 |
| Save Account | สำเร็จ | สำเร็จ |
| Save DepositTransaction | สำเร็จ | สำเร็จ |
| เกิด RuntimeException | เกิด | เกิด |
| Rollback | มี | ไม่มี Transaction ครอบทั้งหมด |
| Account หลัง Error | 5000 | 6000 |
| DepositTransaction หลัง Error | ไม่มีรายการใหม่ | มีรายการใหม่ |
| HTTP Response | 500 | 500 |

จุดสำคัญคือ **HTTP Status อาจเหมือนกัน แต่ข้อมูลใน Database แตกต่างกัน**

---

## 12. การทดสอบด้วย Postman

### 12.1 สร้าง Account

Method:

```http
POST
```

URL:

```text
http://localhost:8080/accounts
```

ตัวอย่าง Request:

```json
{
    "accountNumber": "ACC001",
    "balance": 5000
}
```

หากสร้างสำเร็จ ระบบจะบันทึก Account ลง Database

---

### 12.2 ฝากเงิน

Method:

```http
POST
```

URL:

```text
http://localhost:8080/accounts/1/deposit
```

Body → `raw` → `JSON`

```json
{
    "amount": 1000
}
```

Response เมื่อฝากสำเร็จ:

```http
200 OK
```

```json
{
    "message": "Deposit successful"
}
```

---

## 13. ตรวจสอบยอดเงินหลังฝาก

หลังจากฝากเงิน 1,000 บาท สามารถตรวจสอบ Account ได้ด้วย

```http
GET http://localhost:8080/accounts/1
```

หากยอดเดิมคือ 5,000 บาท ผลลัพธ์ควรเป็นยอดใหม่:

```text
balance = 6000
```

แสดงว่าระบบสามารถเพิ่มและบันทึกยอดเงินใน Account ได้ถูกต้อง

---

## 14. ตรวจสอบ DepositTransaction

สามารถตรวจสอบข้อมูลใน Database ได้จากตาราง `deposit_transaction`

ตัวอย่าง:

| id | amount | account_id |
|---:|---:|---:|
| 1 | 1000 | 1 |

ข้อมูลแสดงว่า

- `id = 1` คือรหัสของรายการฝาก
- `amount = 1000` คือจำนวนเงินที่ฝาก
- `account_id = 1` คือ Account ที่ทำรายการฝาก

---

## 15. การทดลอง Rollback

เพื่อทดสอบ Rollback สามารถเพิ่มคำสั่ง:

```java
throw new RuntimeException("Test Rollback");
```

หลังจาก `save(Account)` และ `save(DepositTransaction)`

```java
accountRepository.save(account);

DepositTransaction deposit =
        new DepositTransaction(amount, account);

depositRepository.save(deposit);

throw new RuntimeException("Test Rollback");
```

เมื่อใช้ `@Transactional`:

```text
Postman
   ↓
POST /accounts/1/deposit
   ↓
500 Internal Server Error
   ↓
ROLLBACK
   ↓
Account กลับเป็นค่าเดิม
   ↓
DepositTransaction รายการใหม่ถูกยกเลิก
```

---

## 16. ผลการทดลอง Rollback

เมื่อทดสอบด้วย Postman จะพบว่า:

```text
500 Internal Server Error
```

เนื่องจากมีการเรียก:

```java
throw new RuntimeException("Test Rollback");
```

แต่หากตรวจสอบ Database จะพบว่า

```text
Account.balance
```

กลับเป็นค่าก่อนการฝากเงิน และ

```text
DepositTransaction
```

ไม่มีรายการใหม่

แสดงว่า `@Transactional` สามารถ Rollback การเปลี่ยนแปลงทั้งหมดภายใน Transaction ได้สำเร็จ

---

## 17. สรุป

โปรเจกต์นี้เป็นระบบฝากเงินที่ใช้ Spring Boot และ Spring Data JPA โดยมี Account สำหรับเก็บยอดเงิน และ DepositTransaction สำหรับเก็บประวัติการฝากเงิน

การฝากเงินประกอบด้วยการเพิ่มยอดเงินใน Account และบันทึกข้อมูล DepositTransaction ซึ่งเป็นการเปลี่ยนแปลงข้อมูลหลายส่วน จึงควรใช้ `@Transactional` เพื่อให้ข้อมูลมีความสอดคล้องกัน

เมื่อทุกขั้นตอนทำงานสำเร็จ ระบบจะทำการ **COMMIT** และบันทึกข้อมูลลง Database แต่หากเกิด `RuntimeException` ระบบจะทำการ **ROLLBACK** การเปลี่ยนแปลงทั้งหมดภายใน Transaction ทำให้ข้อมูลกลับไปเป็นสถานะก่อนเริ่มการฝากเงิน

จากการทดลองเปรียบเทียบพบว่า การมี `@Transactional` ช่วยป้องกันกรณีที่บางข้อมูลถูกบันทึกสำเร็จ ในขณะที่บางขั้นตอนเกิดข้อผิดพลาด ซึ่งอาจทำให้ข้อมูลในระบบไม่สอดคล้องกัน

---

## 18. Key Concepts

```text
@Transactional
     ↓
Transaction Management
     ↓
┌─────────────────────────────┐
│ Find Account                │
│ Update Balance              │
│ Save Account                │
│ Create DepositTransaction   │
│ Save DepositTransaction     │
└─────────────────────────────┘
     ↓
 ┌───────────┐
 │ Success   │ → COMMIT
 └───────────┘

 ┌───────────┐
 │ Error     │ → ROLLBACK
 └───────────┘
```

### หลักการสำคัญ

- **Transaction** — ควบคุมการทำงานของ Database หลายขั้นตอน
- **COMMIT** — ยืนยันการเปลี่ยนแปลง
- **ROLLBACK** — ยกเลิกการเปลี่ยนแปลง
- **`@Transactional`** — กำหนดขอบเขต Transaction ใน Spring
- **Account** — เก็บข้อมูลบัญชีและยอดเงิน
- **DepositTransaction** — เก็บประวัติการฝากเงิน
- **One-to-Many** — Account หนึ่งบัญชีมี DepositTransaction ได้หลายรายการ
- **All or Nothing** — การฝากเงินควรสำเร็จทั้งหมดหรือไม่เปลี่ยนแปลงข้อมูลเลย
