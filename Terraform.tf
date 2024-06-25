terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.36.0"
    }
  }
}

provider "aws" {
  # Configuration options
  region = "us-east-1"
}

# S3 bucket
resource "aws_s3_bucket" "fusion-sre-movies-bucket" {
  bucket_prefix = "fusion-sre-movies-bucket"
}

# DynamoDB

resource "aws_dynamodb_table" "fusion-sre-table" {
  name           = "fusion-sre-table"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "title"

  attribute {
    name = "title"
    type = "S"
  }

}

resource "aws_lambda_function" "s3_to_dynamodb" {
  filename         = "fusion-sre-function.zip"
  function_name    = "s3_to_dynamodb"
  role             = aws_iam_role.lambda_exec.arn
  handler          = "lambda_function.lambda_handler"
  runtime          = "python3.12"
  source_code_hash = filebase64sha256("fusion-sre-function.zip")
  timeout          = 900

  environment {
    variables = {
      TABLE_NAME = aws_dynamodb_table.fusion-sre-table.name
    }
  }
}

resource "aws_iam_role" "lambda_exec" {
  name = "lambda_exec_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Principal = {
          Service = "lambda.amazonaws.com",
        },
        Effect = "Allow",
        Sid    = "",
      }
    ]
  })
}

resource "aws_iam_policy" "lambda_policy" {
  name = "lambda_policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = [
          "dynamodb:PutItem",
          "dynamodb:GetItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem"
        ],
        Effect   = "Allow",
        Resource = aws_dynamodb_table.fusion-sre-table.arn
      },
      {
        Action = [
          "s3:GetObject"
        ],
        Effect   = "Allow",
        Resource = "${aws_s3_bucket.fusion-sre-movies-bucket.arn}/*"
      },
      {
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Effect   = "Allow",
        Resource = "arn:aws:logs:*:*:*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_attach" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = aws_iam_policy.lambda_policy.arn
}

resource "aws_lambda_permission" "allow_s3" {
  statement_id  = "AllowS3Invoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.s3_to_dynamodb.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.fusion-sre-movies-bucket.arn
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.fusion-sre-movies-bucket.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.s3_to_dynamodb.arn
    events              = ["s3:ObjectCreated:*"]
  }
}

variable "objects" {
  type = list(object({
    source = string
    key    = string
  }))

  default = [
    { source = "movies-1900s.json", key = "/movies-1900s.json" },
    { source = "movies-1910s.json", key = "/movies-1910s.json" },
    { source = "movies-1920s.json", key = "/movies-1920s.json" },
    { source = "movies-1930s.json", key = "/movies-1930s.json" },
    { source = "movies-1940s.json", key = "/movies-1940s.json" },
    { source = "movies-1950s.json", key = "/movies-1950s.json" },
    { source = "movies-1960s.json", key = "/movies-1960s.json" },
    { source = "movies-1970s.json", key = "/movies-1970s.json" },
    { source = "movies-1980s.json", key = "/movies-1980s.json" },
    { source = "movies-1990s.json", key = "/movies-1990s.json" },
    { source = "movies-2000s.json", key = "/movies-2000s.json" },
    { source = "movies-2010s.json", key = "/movies-2010s.json" },
    { source = "movies-2020s.json", key = "/movies-2020s.json" }
  ]
}

resource "aws_s3_object" "objects" {
  for_each = { for obj in var.objects : obj.key => obj }

  bucket = aws_s3_bucket.fusion-sre-movies-bucket.bucket
  key    = each.value.key
  source = each.value.source
  acl    = "private"
}





