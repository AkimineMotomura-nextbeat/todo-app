import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { Todo } from '../todo';
import { TodoService } from '../todo.service';

@Component({
  selector: 'app-todo-new',
  templateUrl: './todo-new.component.html',
  styleUrls: ['./todo-new.component.css']
})
export class TodoNewComponent implements OnInit {

  todo: Todo = {id: 0, category: 0, title: "", content: "", state: 0}

  constructor(
    private routes      : ActivatedRoute,
    private location    : Location,
    private todoService : TodoService
  ) { }

  ngOnInit(): void {
  }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    if (this.todo) {
      this.todoService.addTodo(this.todo)
        .subscribe(() => this.goBack());
    }
  }
}
