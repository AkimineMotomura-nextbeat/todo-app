import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { Validators } from '@angular/forms';

import { todoApp } from 'src/app/constant';
import { Todo } from '../../models/todo';
import { Category } from 'src/app/models/category';
import { TodoState } from 'src/app/models/todoState';
import { TodoService } from '../../service/todo.service';
import { CategoryService } from 'src/app/service/category.service';

@Component({
  selector: 'app-category-detail',
  templateUrl: './todo-detail.component.html',
  styleUrls: ['./todo-detail.component.css']
})
export class TodoDetailComponent implements OnInit {

  @Input() todo?: Todo;

  todoForm      : FormGroup;
  categoryList  : Category[] = [];
  stateList     : TodoState[] = [];

  constructor(
    private routes          : ActivatedRoute,
    private location        : Location,
    private todoService     : TodoService,
    private categoryService : CategoryService
  ) { 
    this.todoForm = new FormGroup({
      title     : new FormControl('', Validators.required),
      content   : new FormControl(''),
      category  : new FormControl(todoApp.NO_CATEGORY_ID, Validators.required),
      state     : new FormControl(todoApp.DEFAULT_STATE)
    })
  }

  ngOnInit(): void {
    this.getTodo();
    this.getCategoryList();
    this.getStateList();
  }

  goBack(): void {
    this.location.back();
  }

  getTodo(): void {
    const id = Number(this.routes.snapshot.paramMap.get('id'));
    this.todoService.getTodo(id).subscribe(todo => {
      this.todo = todo;
      this.todoForm.setValue({
        title     : this.todo.title,
        content   : this.todo.content,
        category  : this.todo.category,
        state     : this.todo.state
      })
    })
  }

  getCategoryList(): void {
    this.categoryService.getCategoryList().subscribe(
      categoryList => this.categoryList = categoryList
    )
  }

  getStateList(): void {
    this.todoService.getTodoState().subscribe(
      stateList => this.stateList = stateList
    )
  }

  save(): void {
    if (this.todo && !this.titleForm.invalid) {
      this.todoService.updateTodo({
        id        : this.todo.id,
        category  : Number(this.categoryForm.value),
        title     : this.titleForm.value,
        content   : this.contentForm.value,
        state     : Number(this.stateForm.value)
      }).subscribe(() => this.goBack());
    }
  }

  get titleForm() {
    return this.todoForm.controls['title'];
  }

  get contentForm() {
    return this.todoForm.controls['content'];
  }

  get categoryForm() {
    return this.todoForm.controls['category'];
  }

  get stateForm() {
    return this.todoForm.controls['state'];
  }
}
