CC=g++
CFLAGS= -std=c++23
SRCDIR=src
BUILDDIR=build
TARGET=Dice_Server

SRC=$(shell find $(SRCDIR) -name '*.cpp')
OBJ=$(SRC:$(SRCDIR)/%.cpp=$(BUILDDIR)/%.o)

all: $(TARGET)

$(TARGET): $(OBJ)
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) $^ -o $@

$(BUILDDIR)/%.o: $(SRCDIR)/%.cpp
	@mkdir -p $(@D)
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -rf $(BUILDDIR) $(TARGET)

.PHONY: all clean