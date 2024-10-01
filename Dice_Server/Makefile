CC=gcc
CFLAGS=-Wall -Wextra -std=c2x
SRCDIR=src
BUILDDIR=build
TARGET=$(BUILDDIR)/server

SRC=$(wildcard $(SRCDIR)/**/*.c) $(SRCDIR)/main.c
OBJ=$(SRC:$(SRCDIR)/%.c=$(BUILDDIR)/%.o)

all: $(TARGET)

$(TARGET): $(OBJ)
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) $^ -o $@

$(BUILDDIR)/%.o: $(SRCDIR)/%.c
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -rf $(BUILDDIR)

.PHONY: all clean